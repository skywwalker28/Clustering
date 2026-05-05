package automation.clustering.map;

import automation.clustering.model.DeliveryPoint;

import java.io.FileWriter;
import java.util.List;
import java.util.Map;

import static automation.clustering.optimization.CleanAddress.cleanAddress;

public class RouteMapExporter {

    private static final String[] COLORS = {"#00aa00", "#ff0000", "#0000ff", "#ff6600", "#9900cc"};
    private static final double[] BMM_COORDS = {37.747183, 55.592605};
    private static final double AVG_SPEED = 40.0;
    private static final int UNLOADING_TIME = 15;
    private static final int MANEUVER_TIME = 10;
    private static final double DISTANCE_COEFFICIENT = 1.3; // коэффициент для учета реальных дорог

    private static String escapeJsString(String s) {
        if (s == null) return "";
        return s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("'", "\\'");
    }

    private static String formatDouble(double value) {
        return String.format("%.6f", value).replace(",", ".");
    }

    public static void exportHtmlMap(
            Map<Integer, List<DeliveryPoint>> driverAndPoints,
            String fileName
    ) {
        try (FileWriter writer = new FileWriter(fileName)) {

            writer.write("""
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="utf-8" />
                        <title>Routes Map (MapLibre) - Drag & Drop</title>
                    
                        <script src="https://unpkg.com/maplibre-gl@3.6.1/dist/maplibre-gl.js"></script>
                        <link href="https://unpkg.com/maplibre-gl@3.6.1/dist/maplibre-gl.css" rel="stylesheet" />
                    
                        <style>
                            body { margin: 0; padding: 0; -webkit-tap-highlight-color: transparent; }
                            #map { width: 100%; height: 100vh; }
                    
                            .marker {
                                cursor: grab;
                                width: 32px;
                                height: 32px;
                                border-radius: 50%;
                                display: flex;
                                align-items: center;
                                justify-content: center;
                                font-weight: bold;
                                font-size: 13px;
                                box-shadow: 0 2px 4px rgba(0,0,0,0.3);
                                transition: transform 0.1s ease;
                                user-select: none;
                                -webkit-user-select: none;
                            }
                    
                            .marker:active { cursor: grabbing; }
                    
                            .marker.dragging {
                                opacity: 0.5;
                                transform: scale(1.15);
                                cursor: grabbing;
                                z-index: 1000;
                            }
                    
                            .marker.drop-zone {
                                box-shadow: 0 0 0 3px gold, 0 2px 4px rgba(0,0,0,0.3);
                                transform: scale(1.1);
                            }
                    
                            .marker::after {
                                content: '';
                                position: absolute;
                                top: -12px;
                                left: -12px;
                                right: -12px;
                                bottom: -12px;
                                background: transparent;
                            }
                    
                            .label {
                                background: white;
                                border: 1px solid #333;
                                padding: 1px 4px;
                                border-radius: 3px;
                                font-size: 10px;
                                font-weight: bold;
                                transform: translateY(-28px);
                                white-space: nowrap;
                                box-shadow: 0 1px 2px rgba(0,0,0,0.15);
                                pointer-events: none;
                            }
                    
                            .panel {
                                position: absolute;
                                top: 10px;
                                right: 10px;
                                background: white;
                                padding: 12px 14px;
                                border-radius: 8px;
                                box-shadow: 0 2px 10px rgba(0,0,0,0.25);
                                font-family: Arial;
                                font-size: 14px;
                                z-index: 1000;
                                min-width: 300px;
                                max-height: 80vh;
                                overflow-y: auto;
                            }
                    
                            .panel button {
                                margin-top: 10px;
                                padding: 8px 12px;
                                cursor: pointer;
                                background: #4CAF50;
                                color: white;
                                border: none;
                                border-radius: 4px;
                                width: 100%;
                                font-size: 14px;
                            }
                    
                            .panel button:hover { background: #45a049; }
                    
                            .driver-stats {
                                margin-top: 8px;
                                padding: 8px;
                                border-top: 1px solid #ddd;
                                border-radius: 6px;
                                transition: background 0.2s;
                            }
                    
                            .driver-stats:hover { background: #f5f5f5; }
                    
                            .driver-name {
                                font-weight: bold;
                                margin-bottom: 4px;
                            }
                    
                            .driver-details {
                                font-size: 12px;
                                margin-left: 16px;
                                color: #333;
                            }
                    
                            .driver-meta {
                                font-size: 11px;
                                margin-left: 16px;
                                color: #666;
                                margin-top: 2px;
                            }
                            .distance-highlight {
                                font-weight: bold;
                                color: #2196F3;
                            }
                    
                            .instruction {
                                font-size: 11px;
                                color: #666;
                                margin-top: 8px;
                                padding-top: 6px;
                                border-top: 1px solid #eee;
                                text-align: center;
                            }
                    
                            @media (max-width: 768px) {
                                .panel { font-size: 12px; min-width: 260px; }
                                .marker { width: 28px; height: 28px; font-size: 11px; }
                            }
                        </style>
                    </head>
                    
                    <body>
                    
                    <div id="map"></div>
                    <div id="panel" class="panel">
                        <h4 style="margin: 0 0 8px 0;"> Маршруты (Drag & Drop)</h4>
                        <div id="stats"></div>
                        <div class="instruction">
                            💡 Нажмите на цифру точки для информации<br>
                            💡 Зажмите цифру точки и перетащите для перемещения к другому водителю
                        </div>
                    </div>
                    """);

            writer.write(String.format("""
                    <script>
                        const bmm = [%s, %s];
                        const DISTANCE_COEFFICIENT = %f;
                        let drivers = [];
                        let markers = [];
                        let draggedMarker = null;
                        let draggedDriverIdx = null;
                        let draggedPointIdx = null;
                        let isDragging = false;
                        let map;
                        
                        // Формула гаверсинусов для расчета расстояния между двумя точками (в метрах)
                        function calculateHaversineDistance(point1, point2) {
                            const R = 6371000;
                            const lat1 = point1[1] * Math.PI / 180;
                            const lat2 = point2[1] * Math.PI / 180;
                            const dLat = (point2[1] - point1[1]) * Math.PI / 180;
                            const dLon = (point2[0] - point1[0]) * Math.PI / 180;
                            
                            const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                                Math.cos(lat1) * Math.cos(lat2) *
                                Math.sin(dLon/2) * Math.sin(dLon/2);
                            const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
                            return R * c;
                        }
                        
                        // Расчет полного расстояния маршрута (от базы через все точки)
                        function calculateRouteDistance(driver) {
                            if (driver.points.length === 0) return 0;
                            
                            let totalDistance = 0;
                            let prevCoords = bmm;
                            
                            for (let i = 0; i < driver.points.length; i++) {
                                const currentCoords = driver.points[i].coords;
                                const directDistance = calculateHaversineDistance(prevCoords, currentCoords);
                                totalDistance += directDistance * DISTANCE_COEFFICIENT;
                                prevCoords = currentCoords;
                            }
                            
                            return totalDistance;
                        }
                    
                        function calculateTimeBetween(point1, point2) {
                            const R = 6371;
                            const lat1 = point1[1] * Math.PI / 180;
                            const lat2 = point2[1] * Math.PI / 180;
                            const dLat = (point2[1] - point1[1]) * Math.PI / 180;
                            const dLon = (point2[0] - point1[0]) * Math.PI / 180;
                    
                            const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                                Math.cos(lat1) * Math.cos(lat2) *
                                Math.sin(dLon/2) * Math.sin(dLon/2);
                            const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
                            const distance = R * c;
                    
                            const timeHours = distance / %f;
                            const timeMinutes = timeHours * 60;
                            return timeMinutes + %d + %d;
                        }
                    """,
                    formatDouble(BMM_COORDS[0]),
                    formatDouble(BMM_COORDS[1]),
                    DISTANCE_COEFFICIENT,
                    AVG_SPEED,
                    UNLOADING_TIME,
                    MANEUVER_TIME
            ));

            writer.write("""
                        function calculateDriverStats(driver) {
                            const totalWeight = driver.points.reduce((sum, p) => sum + (p.weight || 0), 0);
                            if (driver.points.length === 0) {
                                return { totalWeight, totalTime: 0, totalDistance: 0 };
                            }
                            let totalTime = 60;
                            let prevCoords = bmm;
                            for (let i = 0; i < driver.points.length; i++) {
                                const currentCoords = driver.points[i].coords;
                                const segmentTime = calculateTimeBetween(prevCoords, currentCoords);
                                totalTime += segmentTime;
                                prevCoords = currentCoords;
                            }
                            const totalDistance = calculateRouteDistance(driver);
                            return { totalWeight, totalTime, totalDistance };
                        }
                    
                        function updateStats() {
                            const statsDiv = document.getElementById('stats');
                            let html = '';
                            drivers.forEach((driver) => {
                                const stats = calculateDriverStats(driver);
                                const hours = Math.floor(stats.totalTime / 60);
                                const minutes = Math.round(stats.totalTime % 60);
                                const distanceKm = stats.totalDistance / 1000;
                                html += `
                                    <div class="driver-stats">
                                        <div class="driver-name">
                                            <span style="display:inline-block;width:12px;height:12px;background:${driver.color};border-radius:50%;margin-right:8px;"></span>
                                            ${driver.name}
                                        </div>
                                        <div class="driver-details">📦 ${stats.totalWeight} кг</div>
                                        <div class="driver-meta">
                                            ⏱ ${hours} ч ${minutes} мин &nbsp;|&nbsp; 
                                             ${driver.points.length} точек &nbsp;|&nbsp;
                                            🛣️ <span class="distance-highlight">${distanceKm < 1 ? Math.round(stats.totalDistance) + ' м' : (distanceKm < 10 ? distanceKm.toFixed(1) + ' км' : Math.round(distanceKm) + ' км')}</span>
                                        </div>
                                    </div>
                                `;
                            });
                            const totalPoints = drivers.reduce((sum, d) => sum + d.points.length, 0);
                            const totalWeightAll = drivers.reduce((sum, d) => sum + calculateDriverStats(d).totalWeight, 0);
                            const totalDistanceAll = drivers.reduce((sum, d) => sum + calculateRouteDistance(d), 0) / 1000;
                            html += `<div style="margin-top:12px; padding-top:8px; border-top:2px solid #ddd;">
                                        <strong> Итого:</strong><br>
                                        📍 ${totalPoints} точек &nbsp;|&nbsp; 
                                        📦 ${totalWeightAll} кг &nbsp;|&nbsp;
                                        🛣️ ${totalDistanceAll < 10 ? totalDistanceAll.toFixed(1) + ' км' : Math.round(totalDistanceAll) + ' км'}
                                    </div>`;
                            statsDiv.innerHTML = html;
                        }
                    
                        function redrawAll() {
                            for (let i = 0; i < 10; i++) {
                                if (map.getLayer('route-line-' + i)) map.removeLayer('route-line-' + i);
                                if (map.getSource('route' + i)) map.removeSource('route' + i);
                            }
                            markers.forEach(m => { if (m && m.remove) m.remove(); });
                            markers = [];
                            drivers.forEach((driver, index) => {
                                if (driver.points.length === 0) return;
                                const routeCoords = [bmm, ...driver.points.map(p => p.coords)];
                                map.addSource('route' + index, {
                                    type: 'geojson',
                                    data: {
                                        type: 'Feature',
                                        geometry: { type: 'LineString', coordinates: routeCoords }
                                    }
                                });
                                map.addLayer({
                                    id: 'route-line-' + index, type: 'line', source: 'route' + index,
                                    paint: { 'line-color': driver.color, 'line-width': 3, 'line-opacity': 0.8 }
                                });
                                driver.points.forEach((point, pointIndex) => {
                                    createMarker(point, driver, index, pointIndex);
                                });
                            });
                            updateStats();
                        }
                    
                        function createMarker(point, driver, driverIndex, pointIndex) {
                            const el = document.createElement('div');
                            el.className = 'marker';
                            el.innerText = point.num;
                            el.style.backgroundColor = driver.color;
                            el.style.color = 'white';
                            el.style.border = '2px solid white';
                            el.dataset.driverIdx = driverIndex;
                            el.dataset.pointIdx = pointIndex;
                            el.dataset.num = point.num;
                    
                            const startDrag = (e) => {
                                e.preventDefault();
                                e.stopPropagation();
                                if (isDragging) return;
                                draggedMarker = el;
                                draggedDriverIdx = parseInt(el.dataset.driverIdx);
                                draggedPointIdx = parseInt(el.dataset.pointIdx);
                                isDragging = true;
                                el.classList.add('dragging');
                                document.addEventListener('mousemove', onDragMove);
                                document.addEventListener('mouseup', onDragEnd);
                                document.addEventListener('touchmove', onDragMove, { passive: false });
                                document.addEventListener('touchend', onDragEnd);
                                document.addEventListener('touchcancel', onDragEnd);
                            };
                    
                            const onClick = (e) => {
                            e.stopPropagation();
                            const popup = new maplibregl.Popup({ offset: 25 })
                                .setLngLat(point.coords)
                                .setHTML(`
                                    <div style="font-family: Arial; min-width: 220px; line-height: 1.3">
                                        <strong>📍 Точка ${point.num}</strong><br>
                                        <div style="font-size: 11px; margin-top: 3px;">${point.address || 'Адрес не указан'}</div>
                                        <div style="font-size: 10px; color: #666; margin-top: 5px;">
                                            🚚 ${driver.name}<br>
                                            ⚖️ Вес: ${point.weight || 0} кг<br>
                                            📍 ${point.coords[1].toFixed(5)}, ${point.coords[0].toFixed(5)}
                                        </div>
                                    </div>
                                `).addTo(map);
                            setTimeout(() => popup.remove(), 30000);
                        };
                    
                            const onDragMove = (e) => {
                                if (!isDragging || !draggedMarker) return;
                                e.preventDefault();
                                let elementUnderCursor = e.touches ? document.elementsFromPoint(e.touches[0].clientX, e.touches[0].clientY) : document.elementsFromPoint(e.clientX, e.clientY);
                                let targetMarker = null;
                                for (let el of elementUnderCursor) {
                                    if (el.classList && el.classList.contains('marker') && el !== draggedMarker) {
                                        targetMarker = el;
                                        break;
                                    }
                                }
                                document.querySelectorAll('.marker').forEach(m => m.classList.remove('drop-zone'));
                                if (targetMarker) targetMarker.classList.add('drop-zone');
                            };
                    
                            const onDragEnd = (e) => {
                                if (!isDragging) return;
                                e.preventDefault();
                                draggedMarker.classList.remove('dragging');
                                let targetMarker = null;
                                const elements = e.changedTouches ? document.elementsFromPoint(e.changedTouches[0].clientX, e.changedTouches[0].clientY) : document.elementsFromPoint(e.clientX, e.clientY);
                                for (let el of elements) {
                                    if (el.classList && el.classList.contains('marker') && el !== draggedMarker) {
                                        targetMarker = el;
                                        break;
                                    }
                                }
                                if (targetMarker && draggedMarker) {
                                    const targetDriverIdx = parseInt(targetMarker.dataset.driverIdx);
                                    const targetPointIdx = parseInt(targetMarker.dataset.pointIdx);
                                    if (draggedDriverIdx !== undefined && draggedPointIdx !== undefined) {
                                        movePoint(draggedDriverIdx, draggedPointIdx, targetDriverIdx, targetPointIdx);
                                    }
                                }
                                document.querySelectorAll('.marker').forEach(m => m.classList.remove('drop-zone'));
                                document.removeEventListener('mousemove', onDragMove);
                                document.removeEventListener('mouseup', onDragEnd);
                                document.removeEventListener('touchmove', onDragMove);
                                document.removeEventListener('touchend', onDragEnd);
                                document.removeEventListener('touchcancel', onDragEnd);
                                draggedMarker = null;
                                draggedDriverIdx = null;
                                draggedPointIdx = null;
                                isDragging = false;
                            };
                    
                            el.addEventListener('mousedown', startDrag);
                            el.addEventListener('touchstart', startDrag, { passive: false });
                            el.addEventListener('click', onClick);
                    
                            const marker = new maplibregl.Marker({ element: el }).setLngLat(point.coords).addTo(map);
                            const label = document.createElement('div');
                            label.className = 'label';
                            label.innerText = point.num;
                            new maplibregl.Marker({ element: label, anchor: 'bottom' }).setLngLat(point.coords).addTo(map);
                            markers.push(marker);
                        }
                    
                        function movePoint(fromDriverIdx, fromPointIdx, targetDriverIdx, targetPointIdx) {
                            if (fromDriverIdx === targetDriverIdx) {
                                return;
                            }
                            
                            const fromDriver = drivers[fromDriverIdx];
                            const targetDriver = drivers[targetDriverIdx];
                        
                            if (fromDriverIdx === targetDriverIdx) {
                                const movingPoint = fromDriver.points[fromPointIdx];
                                fromDriver.points.splice(fromPointIdx, 1);
                                fromDriver.points.splice(targetPointIdx + 1, 0, movingPoint);
                            } else {
                                if (targetDriver.points.length === 1) {
                                    alert('❌ Нельзя забрать единственную точку у ' + targetDriver.name);
                                    return;
                                }
                                const movingPoint = targetDriver.points[targetPointIdx];
                                targetDriver.points.splice(targetPointIdx, 1);
                                fromDriver.points.splice(fromPointIdx + 1, 0, movingPoint);
                            }
                            redrawAll();
                        }
                    
                        function exportChanges() {
                            const exportData = drivers.map(driver => ({
                                name: driver.name,
                                color: driver.color,
                                points: driver.points.map(p => ({
                                    num: p.num,
                                    coords: p.coords,
                                    weight: p.weight,
                                    address: p.address
                                }))
                            }));
                            const dataStr = JSON.stringify(exportData, null, 2);
                            const blob = new Blob([dataStr], {type: 'application/json'});
                            const url = URL.createObjectURL(blob);
                            const a = document.createElement('a');
                            a.href = url;
                            a.download = `routes_${new Date().toISOString().slice(0,19)}.json`;
                            a.click();
                            URL.revokeObjectURL(url);
                            alert('Маршруты экспортированы!');
                        }
                    
                        map = new maplibregl.Map({
                            container: 'map',
                            style: 'https://basemaps.cartocdn.com/gl/positron-gl-style/style.json',
                            center: [37.61, 55.75],
                            zoom: 9
                        });
                    
                        map.on('load', () => {
                            new maplibregl.Marker({color: "#ffcc00"})
                                .setLngLat(bmm)
                                .setPopup(new maplibregl.Popup().setHTML("<strong>🏢 БАЗА (BMM)</strong><br>Отправление всех маршрутов"))
                                .addTo(map);
                    
                            // Данные из Java
                            drivers = """
            );

            // Генерация данных drivers из Java
            writer.write("[");

            int driverIndex = 0;
            int totalDrivers = 0;
            for (Map.Entry<Integer, List<DeliveryPoint>> entry : driverAndPoints.entrySet()) {
                List<DeliveryPoint> route = entry.getValue();
                if (route == null || route.isEmpty()) continue;
                totalDrivers++;
            }

            driverIndex = 0;
            for (Map.Entry<Integer, List<DeliveryPoint>> entry : driverAndPoints.entrySet()) {
                int driverId = entry.getKey();
                List<DeliveryPoint> route = entry.getValue();
                if (route == null || route.isEmpty()) continue;

                String color = COLORS[driverIndex % COLORS.length];
                int driverNumber = driverId + 1;

                writer.write("{\n");
                writer.write("  name: \"Водитель " + driverNumber + "\",\n");
                writer.write("  color: \"" + color + "\",\n");
                writer.write("  points: [\n");

                for (DeliveryPoint point : route) {
                    String address = cleanAddress(point.getAddress());
                    address = escapeJsString(address);
                    writer.write("    {coords: [" + formatDouble(point.getLon()) + ", " +
                            formatDouble(point.getLat()) + "], num: " + point.getNumber() +
                            ", weight: " + point.getWeightKg() +
                            ", address: \"" + address + "\"},\n");
                }

                writer.write("  ]\n");
                writer.write("}" + (driverIndex < totalDrivers - 1 ? ",\n" : "\n"));
                driverIndex++;
            }

            writer.write("""
                            ];
                            redrawAll();
                            document.getElementById('exportBtn').addEventListener('click', exportChanges);
                        });
                    </script>
                    </body>
                    </html>
            """);

            System.out.println("🗺 HTML map created: " + fileName);

        } catch (Exception e) {
            System.err.println("Error creating map: " + e.getMessage());
            e.printStackTrace();
        }
    }
}