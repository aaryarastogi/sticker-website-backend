#!/bin/bash
export EMAIL_ENABLED=true
export MAIL_USERNAME=contactus@stickkery.com
export MAIL_PASSWORD='#Stickkery123'
export MAIL_HOST=smtp.gmail.com
export MAIL_PORT=587
export MAIL_SMTP_AUTH=true
export MAIL_SMTP_STARTTLS=true

cd /Users/aaryarastogi/MERN_Projects/stickerswebsite-backend
mvn spring-boot:run
