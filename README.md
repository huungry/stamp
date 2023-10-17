# Stamp
Stamp is a loyalty stamp-collecting app designed for café enthusiasts.

## Why?
The primary motivation for this project is to experiment with Scala stack. 

**This project is not production ready.** I use it to learn and experiment with different technologies and approaches.

## Tech Stack
- Scala 2.13
- Tapir
- Cats and Cats Effect
- Refined types
- Doobie
- Postgres
- Circe
- Pureconfig
- Chimney
- Test Containers (based on [bootzooka](https://github.com/softwaremill/bootzooka))

## Usage
### Restaurant Employees and Owners
Restaurant owners and managers can configure stamp settings and establish the rewards that customers can earn. 
Additionally, they can authorize specific employees to scan visitors' QR codes for both stamp collection and reward redemption.

### Café Clients
Every time you visit your favorite café and make a purchase, you have the option to show a QR code and earn a stamp. Accumulate enough stamps and you'll unlock rewards.


## Missing Features
- Add missing tests
- Clean build.sbt
- Add docker-compose for local development
- Implement refresh token
- Implement invitation system
- Implement password reset
- Implement upgrading user to Pro (payment service)
- Mobile App :)