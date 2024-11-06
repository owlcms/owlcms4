> [!CAUTION]
>
> - This is an **alpha release**, used for validating new features.  *Some features are likely to be incomplete or non-functional*.
> - **Alpha releases are not normally used in actual competitions.** - It is always wise to export your current database before updating if it contains important data.

- Maintenance log:
  - 54.0.0-alpha01: initial release of scoreboard cleanup
- Scoreboards now correctly display ranks and leaders for categories where medals are given based on a score (see documentation for [score-based medals](https://jflamy.github.io/owlcms4/#/ScoreBasedCompetitions))
- Templates:
  - the athlete's score and ranks in the current category are now obtained by using `${l.categoryScore}` `${l.categoryScoreRank}` (where l is the loop variable giving the current athlete).  If the current category is not score-based, this returns the total and the totalRank.


For other recent changes, see [version 52 release notes](https://github.com/owlcms/owlcms4/releases/tag/52.0.6) and [version 53 release notes](https://github.com/owlcms/owlcms4/releases/tag/53.1.0)
