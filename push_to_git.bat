set timestamp=%DATE:/=-%_%TIME::=-%
set timestamp=%timestamp: =%
set commit_msg='Optimizations @ %timestamp% %1'
echo %commit_msg%
