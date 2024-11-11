> [!CAUTION]
>
> - This is an **alpha release**, used for validating new features.  *Some features are likely to be incomplete or non-functional*.
> - **Alpha releases are not normally used in actual competitions.** - It is always wise to export your current database before updating if it contains important data.

- Maintenance log:
  - 54.0.0-alpha02: reworked the category ranking code for performance when athletes are registered in many eligibility categories.
  - 54.0.0-alpha01: initial release of scoreboard cleanup
- Scoreboards now correctly display ranks and leaders for categories where medals are given based on a score (see documentation for [score-based medals](https://jflamy.github.io/owlcms4/#/ScoreBasedCompetitions))
- Results
  - During a competition with both score-based and total-based rankings, from the Competition Results page, using the Eligibility Categories report with the Score template will produce correct interim or final results.   Each category will be ranked according to it's scoring system.
- Templates:
  - the athlete's score and ranks in the current category are now obtained by using `${l.categoryScore}` `${l.categoryScoreRank}` (where l is the loop variable giving the current athlete).  
  - If the current category is not score-based, this is the same as `${l.total}`and the `${l.totalRank}`, so it is always possible to use the `Score` templates for a total-based competition.
- Bar Loading:
  - The weight under which collars are not used is now configurable.  Default is 40kg.
  - The normal grey bar color is used when 15kg bar is used for women or 20kg is used for men, even if the non-standard bar or children loading rules are in effect.

For other recent changes, see [version 52 release notes](https://github.com/owlcms/owlcms4/releases/tag/52.0.6) and [version 53 release notes](https://github.com/owlcms/owlcms4/releases/tag/53.1.0)
