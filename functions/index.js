'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

// The topic name can be optionally prefixed with "/topics/".
var topic = 'weather';

// See documentation on defining a message payload.

exports.sendFollowerNotification = functions.database.ref('/chat/{id_of_chunk}/')
    .onWrite((change, context) => {
        const id_of_chunk = context.params.id_of_chunk;
        var message = {
            data: {
                chunk_id: id_of_chunk
              },
            topic: id_of_chunk
        };
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

