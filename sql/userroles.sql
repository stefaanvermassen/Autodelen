INSERT INTO `userroles` (`userrole_userid`, `userrole_role`) VALUES 
((SELECT user_id FROM users WHERE user_email='autodelen.zelensis@gmail.com'), 'SUPER_USER');