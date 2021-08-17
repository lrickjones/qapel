import mysql.connector
import pandas as pd

cnx = mysql.connector.connect(user='qapel', database='reader', username="qapel", password="digqap2021")
cursor = cnx.cursor()

query = "SELECT epc, station_id, final_status FROM repository"

cursor.execute(query)

for (epc, station_id, final_status) in cursor:
    print("epc: {}, station_id {}, status {}".format(epc, station_id, final_status))

cursor.close()
cnx.close()