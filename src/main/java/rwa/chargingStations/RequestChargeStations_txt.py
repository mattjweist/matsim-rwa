# Abfrage aller Charging Stations in Deutschland und Abspeichern der Daten

# I M P O R T
import requests

# C R E A T E   R E Q U E S T   S T R I N G
API_KEY = 'f71fe330-a73a-4087-8a46-e9b67b3bc8ff'
UrlRequestChargeStations = f'https://api.openchargemap.io/v3/poi/?output=json&key={API_KEY}' \
                           '&countrycode=DE&minpowerkw=50&connectiontypeid=33&maxresults=5000&usage=public'

# G E T   D A T A   F R O M   O C M   A P I
jsonResponseOCM = requests.get(UrlRequestChargeStations)
ResponseOCM = jsonResponseOCM.json()

# P O S T P R O C E S S I N G

g = open("ChgStations.txt", "w+")

# lastID = 0
for chgstation in ResponseOCM:
    for connection in chgstation['Connections']:
        if (connection['ConnectionTypeID'] == 33) and (connection['Quantity'] is not None):
            format_list = [connection['ID'],
                           chgstation['AddressInfo']['Longitude'],
                           chgstation['AddressInfo']['Latitude'],
                           connection['PowerKW'],
                           connection['Quantity']]
            g.write('{},{},{},{},{}\n'.format(*format_list))

g.close()
g = open("ChgStations.txt", "r")
