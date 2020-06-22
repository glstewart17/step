// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Iterator;


/**
 * Return the possible meeting times that do not conflict with the events the optional
 * and mandatory attendees attending. If there are none, return the time ranges that 
 * work for just the mandatory attendees. 
 */ 
public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

    // Save all attendees that should attend and the duration of the meeting.
    Collection<String> mandatoryAttendees = request.getAttendees();
    Collection<String> optionalAttendees = request.getOptionalAttendees();
    long duration = request.getDuration();

    // Create time ranges which conflict with just mandatory attendees and both.
    ArrayList<TimeRange> mandatoryTimeRanges = new ArrayList<TimeRange>();
    ArrayList<TimeRange> optionalAndMandatoryTimeRanges = new ArrayList<TimeRange>();

    // Iterate over all the events.
    Iterator<Event> eventsIterator = events.iterator();
    while (eventsIterator.hasNext()) {

      // Define event and create a variable to check if added to optional and mandatory.
      Event event = eventsIterator.next();
      boolean addedToOptionalAndMandatory = false;

      // Iterate over all attendees at the event.
      for (String eventAttendee: event.getAttendees()) {
        
        // If attendee in the mandatory attendee arraylist.
        if (mandatoryAttendees.contains(eventAttendee)) {
          
          // If not already added to optional and mandatory time range, add it.
          if (!addedToOptionalAndMandatory) {
            optionalAndMandatoryTimeRanges.add(event.getWhen());
          }

          // Add to mandatory time range and then break.
          mandatoryTimeRanges.add(event.getWhen());
          break;  
        }

        // If attendee in the optional attendee arraylist.
        if (!addedToOptionalAndMandatory && optionalAttendees.contains(eventAttendee)) {

          // Add to optional and mandatory time ranges and record it.
          optionalAndMandatoryTimeRanges.add(event.getWhen());
          addedToOptionalAndMandatory = true;
        }
      }
    }

    // Sort the busy time ranges by start time.
    Collections.sort(mandatoryTimeRanges, TimeRange.ORDER_BY_START);

    // While the start time is before the end of the day, continue adding mandatory meeting times
    // to the mandatory meeting times, which works for just mandatory attendees.  
    int startTime = TimeRange.START_OF_DAY;
    ArrayList<TimeRange> mandatoryMeetingTimes = new ArrayList<TimeRange>();
    while (startTime < TimeRange.END_OF_DAY) {
      
      // If no mandatory times remaining, add remaining window if it fits.
      if (mandatoryTimeRanges.size() == 0) {
        if (startTime + duration <= TimeRange.END_OF_DAY) {
          mandatoryMeetingTimes.add(TimeRange.fromStartEnd(startTime, TimeRange.END_OF_DAY, true));
        }
        startTime = TimeRange.END_OF_DAY;
      }

      // If there is a mandatory time that starts before the start time, find the next start time. 
      else if (mandatoryTimeRanges.get(0).start() < startTime) {
        if (mandatoryTimeRanges.get(0).end() > startTime) {
          startTime = mandatoryTimeRanges.get(0).end();
        }
        mandatoryTimeRanges.remove(0);
      }

      // Otherwise, if there is a large enough window, add it, otherwise, find the next start time.
      else {
        if (startTime + duration <= mandatoryTimeRanges.get(0).start()) {
          mandatoryMeetingTimes.add(
              TimeRange.fromStartEnd(startTime, mandatoryTimeRanges.get(0).start(), false));
        }
        startTime = mandatoryTimeRanges.get(0).end();
        mandatoryTimeRanges.remove(0);
      }
    }

    // Sort the optional and mandatory time ranges by start time.
    Collections.sort(optionalAndMandatoryTimeRanges, TimeRange.ORDER_BY_START);

    // While the start time is before the end of the day, continue adding possible meeting times to
    // to the optional and mandatory meeting times, which works for both optional and mandatory attendees. 
    startTime = TimeRange.START_OF_DAY;
    ArrayList<TimeRange> optionalAndMandatoryMeetingTimes = new ArrayList<TimeRange>();
    while (startTime < TimeRange.END_OF_DAY) {
      
      // If no optional and mandatory times remaining, add remaining window if it fits.
      if (optionalAndMandatoryTimeRanges.size() == 0) {
        if (startTime + duration <= TimeRange.END_OF_DAY) {
          optionalAndMandatoryMeetingTimes.add(
              TimeRange.fromStartEnd(startTime, TimeRange.END_OF_DAY, true));
        }
        startTime = TimeRange.END_OF_DAY;
      }

      // If there is an optional and mandatory time that starts before the start time, add next start time. 
      else if (optionalAndMandatoryTimeRanges.get(0).start() < startTime) {
        if (optionalAndMandatoryTimeRanges.get(0).end() > startTime) {
          startTime = optionalAndMandatoryTimeRanges.get(0).end();
        }
        optionalAndMandatoryTimeRanges.remove(0);
      }

      // Otherwise, if there is a large enough window, add it, otherwise, find the next start time.
      else {
        if (startTime + duration <= optionalAndMandatoryTimeRanges.get(0).start()) {
          optionalAndMandatoryMeetingTimes.add(
              TimeRange.fromStartEnd(startTime, optionalAndMandatoryTimeRanges.get(0).start(), false));
        }
        startTime = optionalAndMandatoryTimeRanges.get(0).end();
        optionalAndMandatoryTimeRanges.remove(0);
      }
    }

    // If there are no mandatory attendees, return optional and mandatory meeting times.
    if (mandatoryAttendees.size() == 0) {
      return optionalAndMandatoryMeetingTimes;
    }

    // If there are no optional and mandatory times, return the just mandatory meeting times.
    else if (optionalAndMandatoryMeetingTimes.size() == 0) {
      return mandatoryMeetingTimes;
    }

    // Otherwise, return the optional and mandatory meeting times.
    return optionalAndMandatoryMeetingTimes;
  }
}
