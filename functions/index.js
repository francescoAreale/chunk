'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

// The topic name can be optionally prefixed with "/topics/".
var topic = 'weather';

// See documentation on defining a message payload.
var message = {
    notification: {
        title: '$GOOG up 1.43% on the day',
        body: '$GOOG gained 11.80 points to close at 835.67, up 1.43% on the day.'
    },
    topic: topic
};

exports.sendFollowerNotification = functions.database.ref('/chat/{}')
    .onWrite((change, context) => {
        // Send a message to devices subscribed to the provided topic.
        admin.messaging().send(message)
            .then((response) => {
                // Response is a message ID string.
                console.log('Successfully sent message:', response);
                return "ben fatto"
            })
            .catch((error) => {
                console.log('Error sending message:', error);
            });
    });

