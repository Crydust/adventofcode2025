Day10Part2
================

Times measured running java 25 on desktop with 5800X cpu and 64 GB ram.

see [performance-desktop.csv](performance-desktop.csv)

## Practical takeaway (based on your measured results)
1. **Enable `USE_PARALLEL_PORTFOLIO`** if your goal is fastest wall-clock time on this machine.
2. **Treat `USE_HYBRID_01` + `USE_ACTIVITY_BASED_SEARCH` as a bundle**: ABS is “worth it” mainly when HYBRID_01 is on.
3. **Don’t expect `USE_DECOMPOSITION_SOLVER` to matter much** for end-to-end runtime.
