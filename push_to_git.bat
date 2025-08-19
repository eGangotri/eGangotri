set timestamp=%DATE:/=-%_%TIME::=-%
set timestamp=%timestamp: =%
set arg1=%1
set arg1WithoutQuotes=%arg1:"='%
set commit_msg="Optimizations at %timestamp% %arg1WithoutQuotes%"
git status
git add src/*
git add .gitignore
git add push_to_git.bat 
git add latestJarForUse/*
git add test/*
git commit -m %commit_msg%
git push origin master
git status

