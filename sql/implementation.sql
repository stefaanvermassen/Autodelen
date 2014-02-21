DROP DATABASE IF EXISTS autodelentest;
CREATE DATABASE autodelentest;
USE autodelentest;

CREATE TABLE `FileGroups` (
	`file_group_id` INT NOT NULL AUTO_INCREMENT,
	PRIMARY KEY (`file_group_id`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `Files` (
	`file_id` INT NOT NULL AUTO_INCREMENT,
	`file_url` VARCHAR(255) NOT NULL,
	`file_file_group_id` INT,
	PRIMARY KEY (`file_id`),
	FOREIGN KEY (`file_file_group_id`) REFERENCES FileGroups(`file_group_id`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `DriverLicenses` (
	`driver_license_id` INT NOT NULL, # Moet gebruiker zelf invullen!
	`driver_license_file` INT NOT NULL,
	PRIMARY KEY (`driver_license_id`),
	FOREIGN KEY (`driver_license_file`) REFERENCES FileGroups(`file_group_id`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `IdentityCards` (
	`identity_card_id` INT NOT NULL, # Identiteitskaartnummer
	`identity_card_registration_nr` INT NOT NULL, # Rijksregisternummer
	`identity_card_file` INT NOT NULL,
	PRIMARY KEY (`identity_card_id`),
	FOREIGN KEY (`identity_card_file`) REFERENCES FileGroups(`file_group_id`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `Addresses` (
  `address_id` INT(10) NOT NULL AUTO_INCREMENT,
  `address_city` VARCHAR(64) NOT NULL,
  `address_zipcode` VARCHAR(12) NOT NULL,
  `address_street` VARCHAR(64) NOT NULL DEFAULT '0',
  `address_street_number` VARCHAR(8) NOT NULL DEFAULT '0',
  `address_street_bus` VARCHAR(4) NULL DEFAULT NULL,
  PRIMARY KEY (`address_id`),
  INDEX `address_place_zip` (`address_city`)
)
  COLLATE='latin1_swedish_ci'
  ENGINE=InnoDB
  AUTO_INCREMENT=2;

CREATE TABLE `Users` (
	`user_id` INT NOT NULL AUTO_INCREMENT,
	`user_email` VARCHAR(64) NOT NULL,
	`user_password` CHAR(64) NOT NULL,
	`user_firstname` VARCHAR(64) NOT NULL,
	`user_lastname` VARCHAR(64) NOT NULL,
	`user_cellphone` VARCHAR(16),
	`user_phone` VARCHAR(16),
	`user_address_domicile_id` INT,
	`user_address_residence_id` INT,
	`user_driver_license_id` INT,
	`user_identity_card_id` INT,
	`user_status` ENUM('VALIDATING', 'REGISTERED', 'INFOSESSION_ENROLLMENT', 'INFOSESSION_PRESENT', 'INFOSESSION_ABSENT', 'DROPPED', 'POTENTIAL', 'FULL') NOT NULL DEFAULT 'REGISTERED', # Stadia die de gebruiker moet doorlopen
	PRIMARY KEY (`user_id`),
	FOREIGN KEY (`user_address_domicile_id`) REFERENCES Addresses(`address_id`),
	FOREIGN KEY (`user_address_residence_id`) REFERENCES Addresses(`address_id`),
	FOREIGN KEY (`user_driver_license_id`) REFERENCES DriverLicenses(`driver_license_id`),
	FOREIGN KEY (`user_identity_card_id`) REFERENCES IdentityCards(`identity_card_id`),
	UNIQUE INDEX `user_email` (`user_email`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `UserRoles` (
	`userrole_userid` INT NOT NULL,
	`userrole_role` ENUM('SUPER_USER','ADMIN') NOT NULL,
	PRIMARY KEY (`userrole_userid`, `userrole_role`),
	FOREIGN KEY (`userrole_userid`) REFERENCES Users(`user_id`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `Cars` (
	`car_id` INT NOT NULL AUTO_INCREMENT,
	`car_type` VARCHAR(64) NOT NULL DEFAULT '0',
	`car_brand` VARCHAR(64) NOT NULL DEFAULT '0',
	`car_location` INT,
	`car_seats` TINYINT UNSIGNED,
	`car_doors` TINYINT UNSIGNED,
	`car_year` DATE,
	`car_gps` BIT(1),
	`car_hook` BIT(1),
	`car_fuel` ENUM('PETROL','DIESEL', 'BIODIESEL', 'GAS', 'HYBRID', 'ELECTRIC'),
	`car_fuel_economy` INT,
	`car_estimated_value` INT,
	`car_owner_annual_km` INT,
	`car_owner_user_id` INT NOT NULL,
	`car_comments` VARCHAR(64),
	`car_last_edit` DATETIME,
	PRIMARY KEY (`car_id`),
	FOREIGN KEY (`car_owner_user_id`) REFERENCES Users(`user_id`) ON DELETE CASCADE,
	FOREIGN KEY (`car_location`) REFERENCES Addresses(`address_id`) ON DELETE CASCADE
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `CarInsurances` (
	`insurance_id` INT NOT NULL AUTO_INCREMENT,
	`car_id` INT NOT NULL,
	`insurance_expiration` DATE NOT NULL,
	`insurance_contract_id` INT NOT NULL DEFAULT '0', # Polisnr
	`insurance_bonus_malus` INT NOT NULL DEFAULT '0',
	PRIMARY KEY (`insurance_id`),
	FOREIGN KEY (`car_id`) REFERENCES Cars(`car_id`) ON DELETE CASCADE
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `TechnicalCarDetails` (
	`details_id` INT NOT NULL AUTO_INCREMENT,
	`car_id` INT NOT NULL,
	`car_license_plate` VARCHAR(64) NOT NULL DEFAULT '0',
	`car_registration` VARCHAR(64) NOT NULL DEFAULT '0',
	`car_chassis_number` INT(17) NOT NULL DEFAULT '0',
	PRIMARY KEY (`details_id`),
	FOREIGN KEY (`car_id`) REFERENCES Cars(`car_id`) ON DELETE CASCADE,
	UNIQUE INDEX `ix_details` (`car_license_plate`, `car_chassis_number`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `CarReservations` (
	`reservation_id` INT NOT NULL AUTO_INCREMENT,
	`reservation_status` ENUM('REQUEST','ACCEPTED', 'REFUSED') NOT NULL DEFAULT 'REQUEST', # Reeds goedgekeurd?
	`reservation_car_id` INT NOT NULL,
	`reservation_user_id` INT NOT NULL,
	`reservation_from` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`reservation_to` TIMESTAMP NOT NULL DEFAULT '0000-00-00 00:00:00',
	PRIMARY KEY (`reservation_id`),
	FOREIGN KEY (`reservation_car_id`) REFERENCES Cars(`car_id`), # Wat moet er gebeuren als de auto verwijderd wordt?
	FOREIGN KEY (`reservation_user_id`) REFERENCES Users(`user_id`) ON DELETE CASCADE
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `Infosessions` (
	`infosession_id` INT NOT NULL AUTO_INCREMENT,
	`infosession_timestamp` TIMESTAMP NOT NULL DEFAULT '0000-00-00 00:00:00',
	`infosession_address_id` INT NOT NULL,
	`infosession_host_user_id` INT,
	PRIMARY KEY (`infosession_id`),
	FOREIGN KEY (`infosession_host_user_id`) REFERENCES Users(`user_id`),
	FOREIGN KEY (`infosession_address_id`) REFERENCES Addresses(`address_id`)	
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `InfosessionEnrollees` ( # Wie is ingeschreven?
	`infosession_id` INT NOT NULL,
	`infosession_enrollee_id` INT NOT NULL,
	`enrollment_status` ENUM('ENROLLED', 'PRESENT', 'ABSENT') NOT NULL DEFAULT 'ENROLLED',
	PRIMARY KEY (`infosession_enrollee_id`),
	FOREIGN KEY (`infosession_enrollee_id`) REFERENCES Users(`user_id`),
	FOREIGN KEY (`infosession_id`) REFERENCES Infosessions(`infosession_id`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `CarPrivileges` (
	`car_privilege_user_id` INT NOT NULL,
	`car_privilege_car_id` INT NOT NULL,
	PRIMARY KEY (`car_privilege_user_id`,`car_privilege_car_id`),
	FOREIGN KEY (`car_privilege_user_id`) REFERENCES Users(`user_id`),
	FOREIGN KEY (`car_privilege_car_id`) REFERENCES Cars(`car_id`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `CarCosts` (
	`car_cost_id` INT NOT NULL AUTO_INCREMENT,
	`car_cost_car_id` INT NOT NULL,
	`car_cost_proof` INT,
	`car_cost_amount` DECIMAL(19,4) NOT NULL,
	`car_cost_description` TEXT,
	`car_cost_status` BIT(1) NOT NULL DEFAULT 0,
	`car_cost_time` DATETIME,
	`car_cost_mileage` DECIMAL(10,1),
	PRIMARY KEY (`car_cost_id`),
	FOREIGN KEY (`car_cost_car_id`) REFERENCES Cars(`car_id`),
	FOREIGN KEY (`car_cost_proof`) REFERENCES FileGroups(`file_group_id`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `CarRides` (
	`car_ride_id` INT NOT NULL AUTO_INCREMENT,
	`car_ride_car_id` INT NOT NULL,
	`car_ride_user_id` INT NOT NULL,
	`car_ride_from` TIMESTAMP NOT NULL,
	`car_ride_to` TIMESTAMP NOT NULL,
	`car_ride_status` BIT(1) NOT NULL DEFAULT 0,
	`car_ride_start_mileage` DECIMAL(10,1),
	`car_ride_end_mileage` DECIMAL(10,1),
	PRIMARY KEY (`car_ride_id`),
	FOREIGN KEY (`car_ride_car_id`) REFERENCES Cars(`car_id`),
	FOREIGN KEY (`car_ride_user_id`) REFERENCES Users(`user_id`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `Messages` (
	`message_id` INT NOT NULL AUTO_INCREMENT,
	`message_from_user_id` INT NOT NULL,
	`message_to_user_id` INT NOT NULL,
	`message_subject` VARCHAR(255),
	`message_body` TEXT,
	`message_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`message_id`),
	FOREIGN KEY (`message_from_user_id`) REFERENCES Users(`user_id`),
	FOREIGN KEY (`message_to_user_id`) REFERENCES Users(`user_id`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;