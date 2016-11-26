/**
 * Created by andreas on 12/11/16.
 */

app.simulationCreationView = Backbone.View.extend({
    el: '#body',
    tile: null,
    value: null,
    draw: null,
    vectorSource: null,
    vectorLayer: null,
    snap: null,
    mapDetailsCOllection: null,
    step: 0,
    nameSim: null,
    sampling_rate: null,
    startHour: null,
    returnHour: null,
    living_feature: null,
    working_feature: null,
    car_percentage: null,
    vehicle_count: null,

    events: {
        'click #previous' : 'previous',
        'click .validModel': 'validModel',
        'click .removeModel': 'cancelUnderCreation',
        'click #cancelCreationSimu': 'cancelCreationSimu'
    },

    initialize: function (options) {
        this.id = options.id;
        console.log('id:' + this.id);
        this.mapDetailsCOllection = new app.collections.mapDetailsCollection({id: this.id});
        console.log(this.mapDetailsCOllection);
        this.render();
        var self = this;
        this.mapDetailsCOllection.on('sync', self.onSync, self);
        this.mapDetailsCOllection.on('add', self.onAddElement, self);
        this.mapDetailsCOllection.on('remove', self.onRemoveElement, self);
    },

    render: function () {
        var self = this;
        this.mapDetailsCOllection.reset();
        $('#content').empty();
        if (this.vectorSource) {
            this.vectorSource.clear();
        }

        //Si la div existait déjà
        if ($('#mapRow').length) {
            $('#mapRow').remove();
        }

        //Ajout de la template au body
        this.$el.append(this.template(new Backbone.Model({"id": this.id})));

        //Fond de carte OSM
        this.tile = new ol.layer.Tile({
            source: new ol.source.OSM()
        });

        //Création de la map, si la variable était déjà inialisé on écrase
        this.map = new ol.Map({
            target: 'map',
            controls: ol.control.defaults({
                attributionOptions: /** @type {olx.control.AttributionOptions} */ ({
                    collapsible: false
                })
            }).extend([
                new ol.control.ScaleLine()
            ]),
            layers: [
                this.tile
            ],
            view: new ol.View({
                center: ol.proj.fromLonLat([-3.459144, 48.732084]),
                zoom: 12
            })
        });

        //Réglage de l'opacité du fond de carte OSM
        this.tile.setOpacity(0.3);

        //Préparation du layer pour notre GeoJSON
        this.vectorSource = new ol.source.Vector();
        this.vectorLayer = new ol.layer.Vector({
            source: this.vectorSource,
            style: function (feature, resolution) {
                return self.generateStyle(feature, resolution);
            }
        });
        this.map.addLayer(this.vectorLayer);

        this.selectPointer = new ol.interaction.Select({
            layers: [this.vectorLayer],
            style: function (feature, resolution) {
                return self.generateSelectStyle(feature, resolution);
            }
        });
        this.selectPointer.on('select', function (e) {
            if (e.deselected.length > 0) {
                self.clickCloseInfo();
            }
            if (e.selected.length > 0) {
                self.renderFeatureCreation(e.selected[0]);
            }
        });
        this.selectPointerMove = new ol.interaction.Select({
            layers: [this.vectorLayer],
            condition: ol.events.condition.pointerMove,
            style: function (feature, resolution) {
                return self.generateSelectMoveStyle(feature, resolution);
            }
        });
        this.snap = new ol.interaction.Snap({
            source: this.vectorSource
        });

        this.fetchCollection();
        $('#modalAvertissementSimulation').modal('show');
        $('#startSim').hide();

        $("#osmSlider").slider({
            orientation: "vertical",
            range: "min",
            min: 0,
            max: 1,
            step: 0.1,
            value: 0.3,
            slide: function (event, ui) {
                self.changeOppacity(ui.value);
            }
        });

        //this.addInteraction();
        this.map.addInteraction(this.selectPointerMove);
        this.map.addInteraction(this.selectPointer);
        this.map.addInteraction(this.snap);
        //Tooltip bootstrap
        $('[data-toggle="tooltip"]').tooltip();

        return this;
    },

    clickCloseInfo: function () {
        $('#osmInfo').empty();
    },

    changeID: function (id) {
        this.id = id;
        this.mapDetailsCOllection.id = id;
    },

    onAddElement: function (element) {
        console.log("add");
        var geojsonModel = element.toGeoJSON();
        var newfeature = new ol.format.GeoJSON().readFeature(geojsonModel, {
            featureProjection: 'EPSG:3857'
        });
        this.vectorSource.addFeature(newfeature);
    },

    onRemoveElement: function (element) {
        console.log("remove");
        $('#osmInfo').empty();
        this.vectorSource.removeFeature(this.vectorSource.getFeatureById(element.attributes.id));
        this.selectPointer.getFeatures().clear();
    },

    onSync: function () {
        console.log('sync');
        /*if (this.mapDetailsCOllection.length > 0) {
         this.vectorSource.clear();
         var geoJson = this.mapDetailsCOllection.toGeoJSON();
         var featuresSource = new ol.format.GeoJSON().readFeatures(geoJson, {
         featureProjection: 'EPSG:3857'
         });
         this.vectorSource.addFeatures(featuresSource);
         this.map.getView().fit(this.vectorSource.getExtent(), this.map.getSize());
         }*/
        var self = this;
        //this.mapDetailsCOllection.on('add', self.onAddElement, self);
    },

    changeOppacity: function (value) {
        this.tile.setOpacity(value);
    },

    fetchCollection: function () {
        var self = this;
        //this.mapDetailsCOllection.off("add");
        this.mapDetailsCOllection.fetch({
            success: function () {
                self.map.getView().fit(self.vectorSource.getExtent(), self.map.getSize());
            }
        });
    },

    generateStyle: function (feature, resolution) {
        var type = feature.getProperties().type;
        var oneway = 1;
        if (feature.getProperties().oneway && feature.getProperties().oneway == true) {
            oneway = 0.5;
        }
        switch (type) {
            case 1:
                //SINGLE ROAD
                var style = new ol.style.Style({
                    stroke: new ol.style.Stroke({
                        color: [241, 196, 15, 1],
                        width: (7 / resolution) * oneway
                    })
                });
                return style;
                break;
            case 2:
                //DOUBLE ROAD
                var style = new ol.style.Style({
                    stroke: new ol.style.Stroke({
                        color: [230, 126, 34, 1],
                        width: (14 / resolution) * oneway
                    })
                });
                return style;
                break;
            case 3:
                //TRIPLE ROAD
                var style = new ol.style.Style({
                    stroke: new ol.style.Stroke({
                        color: [231, 76, 60, 1],
                        width: (21 / resolution) * oneway
                    }),

                });
                return style;
                break;
            case 4:
                var style = new ol.style.Style({
                    fill: new ol.style.Fill({
                        color: [250, 178, 102, 1]
                    }),
                    stroke: new ol.style.Stroke({
                        color: [0, 255, 0, 1],
                        width: 3.5 / resolution
                    })
                });
                return style;
                break;
            case 5:
                //RED_LIGHT
                var style = new ol.style.Style({
                    image: new ol.style.Icon({
                        anchor: [0.5, 0.5],
                        size: [44, 100],
                        offset: [0, 0],
                        opacity: 1,
                        scale: 0.1 / resolution,
                        src: 'assets/img/redlight.jpg'
                    })
                });
                return style;
                break;
            default:
                break;
        }
    },

    generateSelectStyle: function (feature, resolution) {
        var type = feature.getProperties().type;
        var geometry = feature.getGeometry();
        var startCoord = geometry.getFirstCoordinate();
        var endCoord = geometry.getLastCoordinate();
        var oneway = 1;
        var circle = new ol.style.Circle({
            stroke: new ol.style.Stroke({
                color: [50, 50, 50, 1]
            }),
            fill: new ol.style.Fill({
                color: [200, 200, 200, 0.8]
            }),
            radius: 10
        });
        var firstPoint = new ol.style.Style({
            geometry: new ol.geom.Point(startCoord),
            image: circle,
            text: new ol.style.Text({
                textAlign: "center",
                textBaseline: "middle",
                font: 'Normal 12px Arial',
                text: 'A',
                fill: circle.getStroke(),
                offsetX: 0,
                offsetY: 0,
                rotation: 0
            })
        });
        var lastPoint = new ol.style.Style({
            geometry: new ol.geom.Point(endCoord),
            image: circle,
            text: new ol.style.Text({
                textAlign: "center",
                textBaseline: "middle",
                font: 'Normal 12px Arial',
                text: 'B',
                fill: circle.getStroke(),
                offsetX: 0,
                offsetY: 0,
                rotation: 0
            })
        });
        if (feature.getProperties().oneway && feature.getProperties().oneway == true) {
            console.log("oneway true");
            oneway = 0.5;
        }
        switch (type) {
            case 1:
                //SINGLE ROAD
                var styles = [
                    // linestring
                    new ol.style.Style({
                        stroke: new ol.style.Stroke({
                            color: [26, 155, 252, 1],
                            width: ((7 + 2) / resolution) * oneway
                        })
                    }),
                    //First point
                    firstPoint,
                    //Last point
                    lastPoint
                ];
                return styles;
                break;
            case 2:
                //DOUBLE ROAD
                var styles = [
                    // linestring
                    new ol.style.Style({
                        stroke: new ol.style.Stroke({
                            color: [26, 155, 252, 1],
                            width: ((14 + 2) / resolution) * oneway
                        })
                    }),
                    //First point
                    firstPoint,
                    //Last point
                    lastPoint
                ];
                return styles;
                break;
            case 3:
                //TRIPLE ROAD
                var styles = [
                    // linestring
                    new ol.style.Style({
                        stroke: new ol.style.Stroke({
                            color: [26, 155, 252, 1],
                            width: ((21 + 1) / resolution) * oneway
                        })
                    }),
                    //First point
                    firstPoint,
                    //Last point
                    lastPoint
                ];
                return styles;
                break;
            case 4:
                var style = new ol.style.Style({
                    fill: new ol.style.Fill({
                        color: [250, 178, 102, 1]
                    }),
                    stroke: new ol.style.Stroke({
                        color: [26, 155, 252, 1],
                        width: (3.5 + 2) / resolution
                    })
                });
                return style;
                break;
            case 5:
                //RED_LIGHT
                console.log(resolution);
                var style = new ol.style.Style({
                    image: new ol.style.Icon({
                        anchor: [0.5, 0.5],
                        size: [44, 100],
                        offset: [0, 0],
                        opacity: 1,
                        scale: (0.1 + 0.2) / resolution,
                        src: 'assets/img/redlight.jpg'
                    })
                });
                return style;
                break;
            default:
                console.log("default");
                break;
        }
    },

    generateSelectMoveStyle: function (feature, resolution) {
        var type = feature.getProperties().type;
        var oneway = 1;
        if (feature.getProperties().oneway && feature.getProperties().oneway == true) {
            oneway = 0.5;
        }
        switch (type) {
            case 1:
                //SINGLE ROAD
                var style = new ol.style.Style({
                    stroke: new ol.style.Stroke({
                        color: [26, 155, 252, 1],
                        width: (7 / resolution) * oneway
                    })
                });
                return style;
                break;
            case 2:
                //DOUBLE ROAD
                var style = new ol.style.Style({
                    stroke: new ol.style.Stroke({
                        color: [26, 155, 252, 1],
                        width: (14 / resolution) * oneway
                    })
                });
                return style;
                break;
            case 3:
                //TRIPLE ROAD
                var style = new ol.style.Style({
                    stroke: new ol.style.Stroke({
                        color: [26, 155, 252, 1],
                        width: (21 / resolution) * oneway
                    }),

                });
                return style;
                break;
            case 4:
                var style = new ol.style.Style({
                    fill: new ol.style.Fill({
                        color: [26, 155, 252, 1]
                    }),
                    stroke: new ol.style.Stroke({
                        color: [26, 155, 252, 1],
                        width: 3.5 / resolution
                    })
                });
                return style;
                break;
            case 5:
                //RED_LIGHT
                var style = new ol.style.Style({
                    image: new ol.style.Icon({
                        anchor: [0.5, 0.5],
                        size: [44, 100],
                        offset: [0, 0],
                        opacity: 1,
                        scale: (0.1 + 0.2) / resolution,
                        src: 'assets/img/redlight.jpg'
                    })
                });
                return style;
                break;
            default:
                break;
        }
    },

    addInteraction: function () {
        var self = this;
        /*

        this.draw.on('drawend', function (event) {
            console.log('Draw : end');
            var feature = event.feature;
            var JSONFeature = new ol.format.GeoJSON().writeFeature(feature, {
                dataProjection: 'EPSG:3857',
                featureProjection: 'EPSG:3857'
            });
            JSONFeature = JSON.parse(JSONFeature);

            var coord = feature.getGeometry().getCoordinates();
            coord = ol.proj.transform(coord, 'EPSG:3857', 'EPSG:4326');
            JSONFeature.geometry.coordinates = coord;

            if(self.step == 0){ // Premiere étape : lieu d'habitation
                JSONFeature.properties = {type: 6, id:1, name: "Unnamed Habitation Zone", nbHabit: 30, startHour: 10};
                // on doit ouvrir un modal et recup nbhabit et hour
                // On doit afficher la suite si modal OK
                $('#habitZone').hide();
                $('#habitZoneParam').show();
                $('#divPrevious').show();
            } else if(self.step == 2){ // Deuxième étape : lieu de travail
                JSONFeature.properties = {type: 7, id:1, name: "Unnamed Work Zone", returnHour: 10};
                // modal : heure de retour
                $('#workZone').hide();
                $('#workZoneParam').show();
            } else {
                console.log('Start Simulation');
            }

            if (self.step < 4) {
                self.step++;
            }

            self.newModel = new app.models.mapDetailsModel(JSONFeature, {
                parse: true,
                collection: self.mapDetailsCOllection
            });

            self.renderFeatureCreation(self.newModel);

            var geojsonModel = self.newModel.toGeoJSON();
            var newfeature = new ol.format.GeoJSON().readFeature(geojsonModel, {
                featureProjection: 'EPSG:3857'
            });
            self.vectorSource.addFeature(newfeature);

            console.log('step : ' + self.step);

            self.map.removeInteraction(self.draw);
            self.map.removeInteraction(self.snap);
        });*/
    },

    renderFeatureCreation: function (feature) {
        var featureid = feature.getProperties().id;
        var model = this.mapDetailsCOllection.get(featureid);
        switch (this.step) {
            case 0:
                $('#habitZone').hide();
                $('#habitZoneParam').show();
                $('#divPrevious').show();
                this.living_feature = featureid;
                new app.mapPopUpCreateHabitationZoneView({
                    model: model
                });
                break;
            case 2:
                $('#workZone').hide();
                $('#workZoneParam').show();
                this.working_feature = featureid;
                new app.mapPopUpCreateWorkZoneView({
                    model: model
                });
                break;
        }

        if (this.step < 4) {
            this.step++;
        }
        console.log('step : ' + this.step);

        this.map.removeInteraction(this.selectPointerMove);
        this.map.removeInteraction(this.selectPointer);
    },

    validModel: function () {
        switch (this.step) {
            case 1:
                $('#habitZoneParam').hide();
                $('#workZone').show();
                this.startHour = $('#startHour').val();
                this.vehicle_count = parseInt($('#nbHabit').val());
                this.car_percentage = $('#carRepart').val();
                console.log("startHour : " + this.startHour);
                console.log("vehicle_count : " + this.vehicle_count);
                console.log("car_percentage : " + this.car_percentage);
                break;
            case 3:
                $('#workZoneParam').hide();
                $('#startSim').show();
                this.returnHour = parseInt($('#returnHour').val());
                console.log("returnHour : " + this.returnHour);
                break;
        }

        /*
        var model = this.newModel;
        model.unset("id");
        if (this.step == 1) {
            model.set({
                nbHabit: parseInt($('#nbHabit').val()),
                startHour: $('#startHour').val(),
            });

        } else if (model.attributes.type == 7) {
            model.set({
                returnHour: parseInt($('#returnHour').val())
            });
        }
        */

        //a faire pour les 2 models quand lancement de simulation
        /*var self = this;
        model.save(null, {
            success: function () {
                self.mapDetailsCOllection.add(model);
                self.cancelUnderCreation();
            }
        });*/
        $('#osmInfo').empty();

        if (this.step < 4) {
            this.step++;
        }
        //this.addInteraction();
        this.map.addInteraction(this.selectPointerMove);
        this.map.addInteraction(this.selectPointer);
        this.map.addInteraction(this.snap);
        console.log('step : ' + this.step);
        //console.log(model.attributes);
    },

    cancelUnderCreation: function () {
        $('#osmInfo').empty();
        this.value = 'None';
        this.step--;
        console.log('step : ' + this.step);
        this.map.addInteraction(this.selectPointerMove);
        this.map.addInteraction(this.selectPointer);
        this.map.addInteraction(this.snap);
    },

    cancelCreationSimu: function () {
        this.step = 0;
    },

    previous : function (){
        switch (this.step) {
            case 1:
                $('#osmInfo').empty();
                $('#habitZoneParam').hide();
                $('#divPrevious').hide();
                $('#habitZone').show();
                this.living_feature = null;
                this.map.addInteraction(this.selectPointerMove);
                this.map.addInteraction(this.selectPointer);
                this.map.addInteraction(this.snap);
                break;
            case 2:
                $('#workZone').hide();
                $('#divPrevious').hide();
                $('#habitZone').show();
                this.living_feature = null;
                this.startHour = null;
                this.car_percentage = null;
                this.vehicle_count = null;
                this.map.addInteraction(this.selectPointerMove);
                this.map.addInteraction(this.selectPointer);
                this.map.addInteraction(this.snap);
                break;
            case 3:
                $('#osmInfo').empty();
                $('#workZoneParam').hide();
                $('#workZone').show();
                this.working_feature = null;
                this.map.addInteraction(this.selectPointerMove);
                this.map.addInteraction(this.selectPointer);
                this.map.addInteraction(this.snap);
                break;
            case 4:
                $('#startSim').hide();
                $('#workZone').show();
                this.working_feature = null;
                this.returnHour = null;
                this.map.addInteraction(this.selectPointerMove);
                this.map.addInteraction(this.selectPointer);
                this.map.addInteraction(this.snap);
                break;
            default:
                console.log('step > 3 ou < 0');
                break;
        }

        if(this.step > 0){
            if (this.step % 2 == 0) {
                this.step = this.step - 2;
            } else {
                this.step--;
            }
        }
        console.log('cancel, step:'+this.step);
    }
});

