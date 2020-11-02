#!/bin/sh

# Change directory to the directory of the script
cd `dirname $0`

jvmOptions="-Xms256m -Xmx2g"
mainClass=org.optaplanner.quickstarts.distribution.QuickstartsApp
mainClasspath="binaries/*"

echo "Usage: ./runQuickstarts.sh"
echo "Notes:"
echo "- Java 11 or higher must be installed. Get OpenJDK 11 from (https://adoptopenjdk.net/)."
echo "- For JDK, the environment variable JAVA_HOME should be set to the JDK installation directory"
echo "  For example (linux): export JAVA_HOME=/usr/lib/jvm/java-openjdk"
echo "  For example (mac): export JAVA_HOME=/Library/Java/Home"
echo "- The working dir should be the directory of this script."
echo

if [ -f $JAVA_HOME/bin/java ]; then
    echo "Starting quickstarts app with JDK from environment variable JAVA_HOME ($JAVA_HOME)..."
    $JAVA_HOME/bin/java ${jvmOptions} -server -cp ${mainClasspath} ${mainClass} $*
else
    echo "Starting quickstarts app with java from environment variable PATH..."
    java ${jvmOptions} -cp ${mainClasspath} ${mainClass} $*
fi

if [ $? != 0 ] ; then
    echo
    echo "ERROR Failed running the java command."
    echo "Maybe install OpenJDK 11 from (https://adoptopenjdk.net/) and check the environment variable JAVA_HOME ($JAVA_HOME)."
    # Prevent the terminal window to disappear before the user has seen the error message
    read -p "Press [Enter] key to close this window." dummyVar
fi
