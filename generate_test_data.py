import json
import random

random.seed(42)

# Delhi NCR coordinate ranges
LAT_MIN, LAT_MAX = 28.40, 28.85
LON_MIN, LON_MAX = 76.85, 77.50

# Generate 100 vehicles
vehicles = []

for i in range(1, 101):
    vehicles.append({
        "vehicleId": f"TEST_VEH{i:03d}",
        "capacity": random.choice([400, 500, 600, 700, 800]),
        "currentLatitude": round(random.uniform(LAT_MIN, LAT_MAX), 6),
        "currentLongitude": round(random.uniform(LON_MIN, LON_MAX), 6),
        "currentAddress": f"Test Vehicle Location {i}, Delhi NCR, India"
    })

# Generate 1000 orders
orders = []

for i in range(1, 1001):
    orders.append({
        "orderId": f"TEST_ORD{i:04d}",
        "latitude": round(random.uniform(LAT_MIN, LAT_MAX), 6),
        "longitude": round(random.uniform(LON_MIN, LON_MAX), 6),
        "address": f"Test Delivery Location {i}, Delhi NCR, India",
        "packageWeight": round(random.uniform(5, 40), 3),
        "priority": random.choices(
            ["HIGH", "MEDIUM", "LOW"],
            weights=[40, 35, 25]
        )[0]
    })

# Save request payloads
with open("vehicles.json", "w") as file:
    json.dump({"vehicles": vehicles}, file, indent=2)

with open("orders.json", "w") as file:
    json.dump({"orders": orders}, file, indent=2)

print("Test data generated successfully!")
print(f"Vehicles: {len(vehicles)}")
print(f"Orders: {len(orders)}")
print(f"Total order weight: {sum(o['packageWeight'] for o in orders):.3f} kg")
print("Generated: vehicles.json, orders.json")
