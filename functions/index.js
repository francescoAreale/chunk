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

exports.translateMessage = functions.https.onCall((data, context) => {
  // Message text passed from the client.
	const text = data.text;
// Authentication / user information is automatically added to the request.
	const uid = context.auth.uid;
	const name = context.auth.token.name || null;
	const picture = context.auth.token.picture || null;
	const email = context.auth.token.email || null;
	console.log('the user is is:', text);
	return admin.auth().getUser(text)
		.then((userRecord) => {
    	// See the UserRecord reference doc for the contents of userRecord.
    		console.log('Successfully fetched user data:', userRecord['displayName']);
		return userRecord.displayName;
  	})
  	.catch((error) => {
    		console.log('Error fetching user data:', error);
  	});
});
