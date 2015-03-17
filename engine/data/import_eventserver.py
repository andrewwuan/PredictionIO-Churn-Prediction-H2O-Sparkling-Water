"""
Import churn-orange-train data for sparkling water engine
"""

import predictionio
import argparse
import csv

def import_events(client, file):
    f = open(file, 'r')
    parsed = csv.reader(f)

    # Skip header line
    parsed.next()

    # Import users with ID
    customer_id = 1
    for row in parsed:
        client.create_event(
            event = 'customer',
            entity_type = 'customer',
            entity_id = str(customer_id),
            properties = {
                'intl_plan': True if (row[3] == 'Yes') else False,
                'voice_mail_plan': True if (row[4] == 'Yes') else False,
                'num_vmail_msg': int(row[5]),
                'total_day_mins': float(row[6]),
                'total_day_calls': int(row[7]),
                'total_day_charge': float(row[8]),
                'total_eve_mins': float(row[9]),
                'total_eve_calls': int(row[10]),
                'total_eve_charge': float(row[11]),
                'total_night_mins': float(row[12]),
                'total_night_calls': int(row[13]),
                'total_night_charge': float(row[14]),
                'total_intl_mins': float(row[15]),
                'total_intl_calls': int(row[16]),
                'total_intl_charge': float(row[17]),
                'customer_service_calls': int(row[18]),
                'churn': True if (row[19] == 'True') else False
            }
        )
        customer_id += 1

    f.close()
    print "%d users are imported." % (customer_id - 1)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(
        description="Import churn-orange-train data for engine")
    parser.add_argument('--access_key', default='invald_access_key')
    parser.add_argument('--url', default="http://localhost:7070")
    parser.add_argument('--file', default="./data/churn-orange-train.csv")

    args = parser.parse_args()
    print args

    client = predictionio.EventClient(
        access_key=args.access_key,
        url=args.url,
        threads=5,
        qsize=500)
    import_events(client, args.file)
