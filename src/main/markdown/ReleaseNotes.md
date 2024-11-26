> [!WARNING]
>
> - This is a release candidate [(see definition)](https://en.wikipedia.org/wiki/Software_release_life_cycle#Release_candidate), used for final public testing and translation. *It is still a preliminary release*
> - You should test all releases, with actual data, *several days* before a competition. This is especially important when considering the use of a release candidate.

- Maintenance log:
  - 54.0.0-rc05: Age group editing now requires a confirmation if the changes require reallocating categories on existing athletes.  Previously the categories would be removed silently. Changes that do not affect the category boundaries, ages or gender don't require confirmation.
  - 54.0.0-rc05: The "Edit Details" button on the session editing grid now works (same as clicking on the row.)
  - 54.0.0-rc05: Added new properties ageGroup.sortCode and category.sortCodeWithAgeGroup for jxls3 templates using groupBy.
  - 54.0.0-rc05: The document download dialog was not using the current session language for the warning about long processing.
  - 54.0.0-rc05: Translations: Faroese added as new language. Danish translation updated.
  - 54.0.0-rc04: Translations: Romanian, Hungarian. Added "ie" language for temporary validation of Faroese translation
  - 54.0.0-rc03: Fixed error preventing some reports (such as the start list) from being produced.
  - 54.0.0-rc02: Protocol, results and final package spreadsheets
    - In all results spreadsheets, a single best athlete score is shown to avoid controversies when the newer scoring systems give different results than the older ones.  
    - The best athlete system can be selected when producing the results (the default is set in the overall competition rules.) on all three types of documents.
    - Removed the LEGAL paper size from the list.
  - 54.0.0-rc01: Added documentation for interactive editing of championships and age groups
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
-  Templates:
  - the athlete's score and ranks in the current category are now obtained by using `${l.categoryScore}` `${l.categoryScoreRank}` (where l is the loop variable giving the current athlete).  
  - If the current category is not score-based, this is the same as `${l.total}`and the `${l.totalRank}`, so it is always possible to use the `Score` templates for a total-based competition.
  - added new properties ageGroup.sortCode and category.sortCodeWithAgeGroup for templates.  ageGroup.sortCode uses the code, max and min ages.  category.sortCodeWithAgeGroup adds the age group to the sort order - this is used when there are several open championships happening together.
- Bar Loading:
  - The weight under which collars are not used is now configurable.  Default is 40kg.
  - The normal grey bar color is used when 15kg bar is used for women or 20kg is used for men, even if the non-standard bar or children loading rules are in effect.

For other recent changes, see [version 52 release notes](https://github.com/owlcms/owlcms4/releases/tag/52.0.6) and [version 53 release notes](https://github.com/owlcms/owlcms4/releases/tag/53.1.0)
