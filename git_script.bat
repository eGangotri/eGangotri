set timestamp=%DATE:/=-%_%TIME::=-%
set timestamp=%timestamp: =%

git add src/*
git add latestJarForUse/*
git add test/*
git commit -m "Optimizations @ %timestamp%"
git push origin master
git status