> [!WARNING]
>
> - This is a release candidate [(see definition)](https://en.wikipedia.org/wiki/Software_release_life_cycle#Release_candidate), used for final public testing and translation. *It is still a preliminary release*
> - You should test all releases, with actual data, *several days* before a competition. This is especially important when considering the use of a release candidate.

- Maintenance log:
  - 54.0.0-rc10: When editing age groups, changes to the qualifying totals were not being saved
  - 54.0.0-rc10: When using Sinclair to award medals to a group, athletes with no total were still given a (low) score and ranked.
  - 54.0.0-rc10: Marshal screen cogwheel setting for displaying live decision lights works again
  - 54.0.0-rc09: Fix: when a good lift decision was given, and the next athlete would be on the same weight, the announcer lifting order would show the athlete having just lifted at the top with their automatic progression.  This could cause the announcer to prematurely request a change of weight.  Now the update waits until the order has been recomputed and is in sync with the other notifications.
  - 54.0.0-rc09: Added a feature switch "usawCollars" to use collars if available except for U11 and U13 age groups (threshold is ignored)
  - 54.0.0-rc08: Fix: When initially started with no leaderboard, the "main room" public scoreboard was not switching to medals during ceremonies (however the full scoreboard with leaderboard was correct)
  - 54.0.0-rc08: Removed the "show incomplete categories" checkbox from the medal ceremony dialog, as this only makes sense when showing rankings.  Fixed the Rankings page to work when an athlete is removed from a session when the competition has started.
  - 54.0.0-rc07: When opening the announcer "Pause" menu, the computation of the medal ceremony dropdown is now done in the background to avoid delays in large competitions with multiple championships.
  - 54.0.0-rc07: The simulator now sends MQTT events for decisions to be more faithful.  Also, the Clean&Jerk breaks should now reliably stay at 10 seconds during a simulation.
  - 54.0.0-rc07: When using the decision display with keyboard (USB/joystick) devices, there is a *very remote* possibility that events could arrive out of order, causing a decision previously received to go back to no-decision.  This could cause the system to stay stuck on the down signal.  Now such reverting updates will be ignored.
- Speaker
  - The updates to the lifting order grid are now synchronized with the notifications.  Previously the  progression of an a athlete could be visible for a moment, leading the speaker to believe the requested weight was going up to that amount.

- Age Groups and Championships
  - It is now possible to edit interactively the age group settings to define the championship in which the age groups belongs
  - It is now possible to define Championships interactively
  - It is now possible to define that an Age Group awards medals using a scoring system.
  - See the documentation for [score-based medals](https://jflamy.github.io/owlcms4/#/ScoreBasedCompetitions)
  - When changing age boundaries, or bodyweight boundaries, a confirmation is required if there are athletes already assigned to the age group.  This is because the old categories are no longer valid and must be removed. Therefore new categories must be selected for the athletes in the age group, which justifies the need for a confirmation.
- Scoreboards:
  - now correctly display ranks and leaders for categories where medals are given based on a score 
  - Medals scoreboards and medals reports have now been fixed to handle score-based medals and sessions where both traditional and score-based medals are awarded.
  - Changing the medals display shown used for the video stream no longer changes the main screen
  - The "public" scoreboard meant to be used in the main room correctly switches during medal ceremonies
- Results
  - During a competition with both score-based and total-based rankings, from the Competition Results page, using the Eligibility Categories report with the Score template will produce correct interim or final results.   Each category will be ranked according to it's scoring system.
  - Updated the competition results and the protocol sheets to use the faster jxls3 template processing. The categories are now listed in alphabetical order.
  - Athletes that did not weigh-in for their session no longer interfere with the determination that their categories are done and ready to receive medals.
  - In all results spreadsheets, a single best athlete score is shown to avoid controversies when the newer scoring systems give different results than the older ones.  
  - The best athlete system can be selected when producing the results (the default is set in the overall competition rules.) on all three types of documents.
- Templates:
  - the athlete's score and ranks in the current category are now obtained by using `${l.categoryScore}` `${l.categoryScoreRank}` (where l is the loop variable giving the current athlete).  
  - If the current category is not score-based, this is the same as `${l.total}`and the `${l.totalRank}`, so it is always possible to use the `Score` templates for a total-based competition.
  - added new properties 
    - ageGroup.sortCode and category.sortCodeWithAgeGroup for templates.  ageGroup.sortCode uses the code, max and min ages.  category.sortCodeWithAgeGroup adds the age group to the sort order - this is used when there are several open championships happening together.
    - athlete.gender.translatedGenderCode now returns the translation (for example, W instead of F)
  - Removed the LEGAL paper size from the list.
- Bar Loading:
  - The weight under which collars are not used is now configurable.  Default is 40kg.
  - The normal grey bar color is used when 15kg bar is used for women or 20kg is used for men, even if the non-standard bar or children loading rules are in effect.

For other recent changes, see [version 52 release notes](https://github.com/owlcms/owlcms4/releases/tag/52.0.6) and [version 53 release notes](https://github.com/owlcms/owlcms4/releases/tag/53.1.0)
