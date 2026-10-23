# Trippel Wallet

Åpner Google Wallet når av/på-knappen trykkes 3 ganger raskt.

## Bygg og installer
1. Installer Android Studio (gratis) og åpne denne mappen (File > Open).
2. La Android Studio synkronisere Gradle ferdig.
3. På telefonen: Innstillinger > Om telefonen > Programvareinformasjon > trykk 7 ganger på "Byggnummer".
   Deretter: Utvikleralternativer > slå på USB-feilsøking.
4. Koble telefonen til PC med USB, velg telefonen øverst i Android Studio og trykk Run (grønn trekant).

## Oppsett i appen
1. Tillat "Vis over andre apper" (påkrevd).
2. Slå av batterioptimalisering.
3. Tillat varsel.
4. Trykk Start.

## Samsung: hindre at appen blir stoppet
Innstillinger > Batteri > Bakgrunnsbegrensninger > Apper som aldri skal sove > legg til Trippel Wallet.

## Justering
Tidsvindu og antall trykk endres i PressService.kt (WINDOW_MS og PRESSES).
