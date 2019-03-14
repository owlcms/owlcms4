/*
 * Copyright 2009-2012, Jean-François Lamy
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package app.owlcms.spreadsheet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.UI;

import app.owlcms.data.athlete.Athlete;
import app.owlcms.data.athlete.AthleteRepository;
import app.owlcms.data.athleteSort.AthleteSorter;
import app.owlcms.data.competition.Competition;
import app.owlcms.data.group.Group;
import app.owlcms.i18n.Messages;

/**
 * @author jflamy
 *
 */
@SuppressWarnings("serial")
public class JXLSTimingStats extends JXLSWorkbookStreamSource {

    public class SessionStats {

        @Override
        public String toString() {
            double hours = (maxTime.getTime()-minTime.getTime())/1000.0/60.0/60.0;
            return "SessionStats [groupName=" + getGroupName() + ", nbAthletes=" + nbAthletes + ", minTime=" + minTime + ", maxTime=" + maxTime
                    + ", nbAttemptedLifts=" + nbAttemptedLifts + " Hours=" + hours+ " AthletesPerHour=" + nbAthletes/hours+ "]" ;
        }

        String groupName = null;
        int nbAthletes;
        Date maxTime = new Date(0L); // forever ago
        Date minTime = new Date(); // now
        int nbAttemptedLifts;

        public SessionStats() {
        }

        public SessionStats(String groupName) {
            this.setGroupName(groupName);
        }

        public Date getMaxTime() {
            return maxTime;
        }

        public Date getMinTime() {
            return minTime;
        }

        public int getNbAttemptedLifts() {
            return nbAttemptedLifts;
        }

        public int getNbAthletes() {
            return nbAthletes;
        }

        public void setMaxTime(Date maxTime) {
            this.maxTime = maxTime;
        }

        public void setMinTime(Date minTime) {
            this.minTime = minTime;
        };

        public void setNbAttemptedLifts(int nbAttemptedLifts) {
            this.nbAttemptedLifts = nbAttemptedLifts;
        }

        public void setNbAthletes(int nbAthletes) {
            this.nbAthletes = nbAthletes;
        }

        public void updateMaxTime(Date newTime) {
            if (this.maxTime.compareTo(newTime) < 0) {
//                System.err.println("updateMaxTime updating "+newTime+" later than "+this.maxTime);
                this.maxTime = newTime;
            } else {
//                System.err.println("updateMaxTime not updating: "+newTime+" earlier than "+this.maxTime);
            }

        }

        public void updateMinTime(Date newTime) {
            if (this.minTime.compareTo(newTime) > 0) {
//                System.err.println("updateMinTime updating: "+newTime+" earlier than "+this.minTime);
                this.minTime = newTime;
            } else {
//                System.err.println("updateMinTime not updating: "+newTime+" later than "+this.minTime);
            }

        }

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }
    }

    Logger logger = LoggerFactory.getLogger(JXLSTimingStats.class);

    public JXLSTimingStats() {
        super(false);
    }

    public JXLSTimingStats(boolean excludeNotWeighed) {
        super(excludeNotWeighed);
    }

    @Override
    protected void getSortedAthletes() {
        HashMap<String, Object> reportingBeans = getReportingBeans();

        this.athletes = AthleteSorter.registrationOrderCopy(AthleteRepository.findAllByGroupAndWeighIn(null,isExcludeNotWeighed()));
        if (athletes.isEmpty()) {
            // prevent outputting silliness.
            throw new RuntimeException(Messages.getString("OutputSheet.EmptySpreadsheet", UI.getCurrent().getLocale())); //$NON-NLS-1$
        }

        // extract group stats
        Group curGroup = null;
        Group prevGroup = null;

        List<SessionStats> sessions = new LinkedList<SessionStats>();

        SessionStats curStat = null;
        for (Athlete curAthlete : athletes) {
            curGroup = curAthlete.getGroup();
            if (curGroup == null) {
                continue;  // we simply skip over athletes with no groups
            }
            if (curGroup != prevGroup) {
                processGroup(sessions, curStat);

                String name = curGroup.getName();
                curStat = new SessionStats(name);
            }
            // update stats, min, max.
            curStat.setNbAthletes(curStat.getNbAthletes() + 1);
            Date minTime = curAthlete.getFirstAttemptedLiftTime();
            curStat.updateMinTime(minTime);

            Date maxTime = curAthlete.getLastAttemptedLiftTime();
            curStat.updateMaxTime(maxTime);

            int nbAttemptedLifts = curAthlete.getAttemptedLifts();
            curStat.setNbAttemptedLifts(curStat.getNbAttemptedLifts() + nbAttemptedLifts);

            prevGroup = curGroup;
        }
        if (curStat.getNbAthletes() > 0) {
            processGroup(sessions, curStat);
        }
        reportingBeans.put("groups", sessions);
    }

    @Override
    public InputStream getTemplate() throws IOException {
        String templateName = "/TimingStatsTemplate_" + UI.getCurrent().getLocale().getLanguage() + ".xls";
        final InputStream resourceAsStream = this.getClass().getResourceAsStream(templateName);
        if (resourceAsStream == null) {
            throw new IOException("resource not found: " + templateName);} //$NON-NLS-1$
        return resourceAsStream;
    }

    @Override
    protected void init() {
        super.init();

        Competition competition = Competition.getCurrent();
        getReportingBeans().put("competition", competition);

    }

    private void processGroup(List<SessionStats> sessions, SessionStats curStat) {
        if (curStat == null) return;
        //System.err.println(curStat.toString());
        sessions.add(curStat);
    }

}
