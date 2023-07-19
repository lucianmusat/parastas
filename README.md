# Parastas

A simple docker container watcher to keep an eye out for running containers and notify when any of them go down.

![How it looks](screenshot.png)

### Why?

I have some applications running in docker containers on my local raspberry pi and sometimes some of them would 
error out and stop without me knowing and I wouldn't realise for weeks until I check them out.
I could have done a simple bash script/cron job to monitor but I thought this would be a good opportunity to
start a small Spring Framework project and create a lightweight monitoring tool for my containers.

### How to use

- Make sure you have you have docker expose daemon on localhost:2375
- Clone the project
- run `mvn clean install`
- Got to Settings and set up your SMTP
- docker compose up -d
- open up localhost:8080 and if everything is ok you should see the list of running docker containers
- check the ones you want to monitor

### NOTE
** I using Gmail SMTP server don't forget to "allow less secure apps" in you Gmail settings or create an "app password" if you have 2FA enabled.**

### NOTE

This is a work in progress. Depending on how far along I am with the features I plan to implement it might not
be of any use for anyone else.

### TODO:

 - [X] Run in Docker container
 - [X] Add email notifications functionality.
 - [X] Add settings page where you can configure your email and check frequency.
 - [X] Make UI nicer.
 - [ ] Update status icons for containers in the UI based on status.
