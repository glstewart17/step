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
    long duration = request.getDuration();

    // Add all time ranges that someone who is in the meeting will be in to an arrayList.
    ArrayList<TimeRange> busyTimeRanges = new ArrayList<TimeRange>();
    Iterator<Event> eventsIterator = events.iterator();
    while (eventsIterator.hasNext()) {
      Event event = eventsIterator.next();
      for (String eventAttendee: event.getAttendees())
        if (attendees.contains(eventAttendee)) {
          busyTimeRanges.add(event.getWhen());
          break;  
        }
    }

    // Sort the busy time ranges by start time.
    Collections.sort(busyTimeRanges, TimeRange.ORDER_BY_START);

    // While the start time is before the end of the day, continue adding possible meeting times. 
    int startTime = TimeRange.START_OF_DAY;
    ArrayList<TimeRange> possibleMeetingTimes = new ArrayList<TimeRange>();
    while (startTime < TimeRange.END_OF_DAY) {
      
      // If no busy times remaining, add remaining window if it fits.
      if (busyTimeRanges.size() == 0) {
        if (startTime + duration <= TimeRange.END_OF_DAY) {
          possibleMeetingTimes.add(TimeRange.fromStartEnd(startTime, TimeRange.END_OF_DAY, true));
        }
        startTime = TimeRange.END_OF_DAY;
      }

      // If there is a busy time that starts before the start time, find the next start time. 
      else if (busyTimeRanges.get(0).start() < startTime) {
        if (busyTimeRanges.get(0).end() > startTime) {
          startTime = busyTimeRanges.get(0).end();
        }
        busyTimeRanges.remove(0);
      }

      // Otherwise, if there is a large enough window, add it, otherwise, find the next start time.
      else {
        if (startTime + duration <= busyTimeRanges.get(0).start()) {
          possibleMeetingTimes.add(TimeRange.fromStartEnd(startTime, busyTimeRanges.get(0).start(), false)) ;
        }
        startTime = busyTimeRanges.get(0).end();
        busyTimeRanges.remove(0);
      }
    } 

    // Return all possible meeting times.
    return possibleMeetingTimes;
  }
}
