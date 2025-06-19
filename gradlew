#!/bin/sh
GRADLE_OPTS="$GRADLE_OPTS \"-Xdock:name=order-management\" \"-Xdock:icon=$APP_HOME/media/gradle.icns\""
exec gradle "$@"
