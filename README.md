# Lagom ScalaJs Example

An example of how to use the Lagom ScalaJs client prototype.

### Getting Started

The Lagom ScalaJs client is currently a prototype branch of Lagom. You'll need to publish it locally to test it out.

1. Clone the Lagom prototype Git repo:

   ```
   git clone -b feature/scalajs https://github.com/mliarakos/lagom.git
   ```
1. Publish Lagom locally:

   ```
   sbt +publishLocal
   ```
   
   This will replace your local `1.6.0-SNAPSHOT` version of Lagom. 
1. Clone the example repo:

   ```
   git clone https://github.com/mliarakos/lagom-scalajs-example.git
   ```
1. Start the example:

   ```
   sbt runAll
   ```
1. Open http://localhost:53781 in a browser 