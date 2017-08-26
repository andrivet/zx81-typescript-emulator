#!/usr/bin/env bash

echo Redirect port 8000 to 63342 - webStorm web server
socat TCP-LISTEN:8000,fork TCP:localhost:63342


