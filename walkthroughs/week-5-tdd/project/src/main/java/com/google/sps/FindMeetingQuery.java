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

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

    // Save all attendees that should attend and the duration of the meeting.
    Collection<String> attendees = request.getAttendees();
    Collection<String> optionalAttendees = request.getOptionalAttendees();
    long duration = request.getDuration();

    // Add all time ranges that someone who is in the meeting will be in to the correct
    // mandatory, and optional and mandatory time range arrayLists.
    ArrayList<TimeRange> mandatoryTimeRanges = new ArrayList<TimeRange>();
    ArrayList<TimeRange> optionalMandatoryTimeRanges = new ArrayList<TimeRange>();
    Iterator<Event> eventsIterator = events.iterator();
    while (eventsIterator.hasNext()) {
      Event event = eventsIterator.next();
      boolean addedToOptional = false;
      for (String eventAttendee: event.getAttendees()) {
        if (attendees.contains(eventAttendee)) {
          mandatoryTimeRanges.add(event.getWhen());
          optionalMandatoryTimeRanges.add(event.getWhen());
          break;  
        }
        if (!addedToOptional && optionalAttendees.contains(eventAttendee)) {
          optionalMandatoryTimeRanges.add(event.getWhen());
          addedToOptional = true;
        }
      }
    }

    // Sort the busy time ranges by start time.
    Collections.sort(mandatoryTimeRanges, TimeRange.ORDER_BY_START);

    // While the start time is before the end of the day, continue adding mandatory meeting times. 
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
    Collections.sort(optionalMandatoryTimeRanges, TimeRange.ORDER_BY_START);

    // While the start time is before the end of the day, continue adding possible meeting times. 
    startTime = TimeRange.START_OF_DAY;
    ArrayList<TimeRange> optionalMandatoryMeetingTimes = new ArrayList<TimeRange>();
    while (startTime < TimeRange.END_OF_DAY) {
      
      // If no optional and mandatory times remaining, add remaining window if it fits.
      if (optionalMandatoryTimeRanges.size() == 0) {
        if (startTime + duration <= TimeRange.END_OF_DAY) {
          optionalMandatoryMeetingTimes.add(
              TimeRange.fromStartEnd(startTime, TimeRange.END_OF_DAY, true));
        }
        startTime = TimeRange.END_OF_DAY;
      }

      // If there is an optional or mandatory time that starts before the start time, add next start time. 
      else if (optionalMandatoryTimeRanges.get(0).start() < startTime) {
        if (optionalMandatoryTimeRanges.get(0).end() > startTime) {
          startTime = optionalMandatoryTimeRanges.get(0).end();
        }
        optionalMandatoryTimeRanges.remove(0);
      }

      // Otherwise, if there is a large enough window, add it, otherwise, find the next start time.
      else {
        if (startTime + duration <= optionalMandatoryTimeRanges.get(0).start()) {
          optionalMandatoryMeetingTimes.add(
              TimeRange.fromStartEnd(startTime, optionalMandatoryTimeRanges.get(0).start(), false));
        }
        startTime = optionalMandatoryTimeRanges.get(0).end();
        optionalMandatoryTimeRanges.remove(0);
      }
    }

    // If there are no attendees, return optional and mandatory times.
    if (attendees.size() == 0) {
      return optionalMandatoryMeetingTimes;
    }

    // If there are no optional and mandatory times, return the mandatory times.
    else if (optionalMandatoryMeetingTimes.size() == 0) {
      return mandatoryMeetingTimes;
    }

    // Otherwise, return optional and mandatory times.
    return optionalMandatoryMeetingTimes;
  }
}
