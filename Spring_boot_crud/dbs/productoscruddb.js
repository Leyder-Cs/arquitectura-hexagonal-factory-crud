// NOMBRE DE DB: use productoscruddb
db.products.insertMany([
    { _id: NumberInt(1), name: 'Portátil Asus', price: 2500000, description: 'Equipo de ejemplo para programación' },
    { _id: NumberInt(2), name: 'Mouse Logitech', price: 45000, description: 'Mouse USB' },
    { _id: NumberInt(3), name: 'Teclado Mecanico', price: 85000, description: 'Teclado para el laboratorio' }
])
db.counters.insertOne({ _id: 'products', seq: NumberInt(3) })