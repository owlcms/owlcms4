> [!WARNING]
>
> - This is a **beta release**, used for testing and translation. ***Some features could be non-functional***.
> - Beta releases are **not** normally used in actual competitions, except when a new feature is required. Use extreme care in testing if you intend to do so.

- Maintenance log:
  - 54.2.0: Experimental feature to stop or restart the system from the web interface.  Currently shown on the home page if the feature switch `manageOwlcms` is present.
  - 54.1.1: Ages are now always calculated relative to the competition date.
  - 54.1.0: Removed the "sinclair meet" option, see Score-based Competitions below
  - 54.1.0: The program was giving a spurious warning when a late change was made between the time the next weight was displayed and the time the clock  started running.
  
- Score-based Competitions
  -  Removed the "Sinclair Meet" option from the competition options.  You can achieve the same using [score-based medals](https://jflamy.github.io/owlcms4/#/ScoreBasedCompetitions):
    - Create SM and SF age groups with ages 0-999 and only the default weight category.  Select "Already Gendered"
  
    - Select "Sinclair" or "Q-Points" or what you want as the scoring system. 
  
    - Unselect the traditional categories and reallocate athletes.
  
    - You can also create separate SM and SF for ages 0-34 and MM and MF for ages 35-999.  MM and MF would have SMHF or Q-masters as scoring system.  Same idea if you want youth categories to be score-based,  create additional YM and YF with Q-youth etc.
  
- Speaker
  - The updates to the lifting order grid are now synchronized with the notifications.  Previously the progression of an athlete could be visible for a moment, leading the speaker to believe the requested weight was going up to that amount.

- Age Groups and Championships
  - It is now possible to edit interactively the age group settings to define the championship in which the age groups belongs
  - It is now possible to define Championships interactively
  - It is now possible to define that an Age Group awards medals using a scoring system.
  - See the documentation for [score-based medals](https://jflamy.github.io/owlcms4/#/ScoreBasedCompetitions)
  - When changing age boundaries, or bodyweight boundaries, a confirmation is required if there are athletes already assigned to the age group.  This is because the old categories are no longer valid and must be removed. Therefore new categories must be selected for the athletes in the age group, which justifies the need for a confirmation.
- Down Signal and Decisions
  - When using the decision display with keyboard (USB/joystick) devices, there was a *very remote* possibility that events could arrive out of order, causing the system to stay stuck on the down signal.  Now such reverting updates will be ignored.
- Scoreboards:
  - Now correctly display ranks and leaders for categories where medals are given based on a score 
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
  -  Added a feature switch "usawCollars" to use collars if available except for U11 and U13 age groups (threshold is ignored)

For other recent changes, see [version 52 release notes](https://github.com/owlcms/owlcms4/releases/tag/52.0.6) and [version 53 release notes](https://github.com/owlcms/owlcms4/releases/tag/53.1.0)
