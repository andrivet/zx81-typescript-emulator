#!/usr/bin/env bash

socat TCP-LISTEN:8000,fork TCP:localhost:63342

