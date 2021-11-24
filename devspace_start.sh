#!/bin/bash
set +e  # Continue on errors

# Remove http blocker
cp settings.xml /usr/share/maven/conf/settings.xml

echo "Installing maven dependencies"
mvn -T 1C install

COLOR_CYAN="\033[0;36m"
COLOR_RESET="\033[0m"

echo -e "${COLOR_CYAN}
   ____              ____
  |  _ \  _____   __/ ___| _ __   __ _  ___ ___
  | | | |/ _ \ \ / /\___ \| '_ \ / _\` |/ __/ _ \\
  | |_| |  __/\ V /  ___) | |_) | (_| | (_|  __/
  |____/ \___| \_/  |____/| .__/ \__,_|\___\___|
                          |_|
${COLOR_RESET}
Welcome to your development container!

This is how you can work with it
- Run \`${COLOR_CYAN}./build.sh run${COLOR_RESET}\` to run the application
- ${COLOR_CYAN}Files will be synchronized${COLOR_RESET} between your local machine and this container
"

bash
