(function(app) {
  app.AppComponent =
    ng.core.Component({
      selector: 'my-app',
      template: '<div><img src="img/temp.png" /> <span>{{temperature}}</span> </div> \
      <div><img src="img/press.png" /> <span>{{pressure}}</span>   </div> \
      <div> <img src="img/humi.png" /> <span>{{humidility}}</span>  </div> \
      <div><img src="img/wind.png" /> <span>{{wind}}</span> </div> \
      <div><img src="img/sun.png" /> <span>{{sun}}</span> </div>'
    })
    .Class({
      constructor: function() {
        this.temperature = '23';
        this.pressure = 1002;
        this.humidility = 80;
        this.wind = 5;
        this.sun = 4;
      },

    });
})(window.app || (window.app = {}));
