echo off
set arg1=%*

set GIT_COMMITTER_NAME="anechka-k"
set GIT_COMMITTER_EMAIL="anechka.k123@yandex.com"
git add --all .
git commit -m "%arg1%" --author="anechka-k <anechka.k123@yandex.com>"