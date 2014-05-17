INSERT INTO `userroles` (`userrole_userid`, `userrole_role`) VALUES 
((SELECT user_id FROM users WHERE user_email='stefaanvermassen@zelensis.be'), 'SUPER_USER');