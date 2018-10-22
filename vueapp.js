	Vue.use(HighchartsVue.default);
	
	
	function onChartLoad() {

		var centerX = 50,
			centerY = 50;

		// Big 5
		big5 = this.renderer.text('5')
			.attr({
				zIndex: 6
			})
			.css({
				color: 'red',
				fontSize: '100px',
				fontStyle: 'italic',
				fontFamily: '\'Brush Script MT\', sans-serif'
			})
			.add();
		big5.attr({
			x: centerX - big5.getBBox().width / 2,
			y: centerY + 14
		});

	}
	
	function fetchData(url, name, obj) {
			fetch(url)
			  .then(
				function(response) {
				  if (response.status !== 200) {
					console.log('Looks like there was a problem. Status Code: ' + response.status);
					return;
				  }

				  // Examine the text in the response
				  response.json().then(function(data) {
					console.log(data);
					obj.chartOptions.stockChart.series.push( {
						name: name,
						data: data
					});					
				  });
				}
			  )
			  .catch(function(err) {
				console.log('Fetch Error :-S', err);
			  });				
	}
	
	
	
	var options = {
		splineChart: {
			chart: {
			  type: 'spline'
			},
			title: {
			  text: 'Entire title'
			},
			credits: {
				enabled: false
			},					
			series: [{
			  data: [10, 0, 8, 2, 6, 4, 5, 5]
			}]
		},
		stockChart: {
			//chart: {
				//type: 'line'
				//type: 'flags'
			//},
			title: {
			  text: 'Stock'
			},
			credits: {
				enabled: false
			},					
			rangeSelector: {
				selected: 4
			},
			yAxis: {
				labels: {
					formatter: function () {
						return (this.value > 0 ? ' + ' : '') + this.value + '%';
					}
				},
				plotLines: [{
					value: 0,
					width: 2,
					color: 'silver'
				}]
			},
			plotOptions: {
				series: {
					compare: 'percent',
					showInNavigator: true
				}
			},
			tooltip: {
				pointFormat: '<span style="color:{series.color}">{series.name}</span>: <b>{point.y}</b> ({point.change}%)<br/>',
				valueDecimals: 2,
				split: true
			},
			series: [
			]
		},
		pieChart: {
			chart: {
				plotBackgroundColor: null,
				plotBorderWidth: null,
				plotShadow: false,
				type: 'pie',
				events: {
					load: onChartLoad
				}						
			},
			title: {
				text: 'Browser market shares in January, 2018'
			},
			credits: {
				enabled: false
			},					
			tooltip: {
				pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
			},
			plotOptions: {
				pie: {
					allowPointSelect: true,
					cursor: 'pointer',
					dataLabels: {
						enabled: true,
						format: '<b>{point.name}</b>: {point.percentage:.1f} %',
						style: {
							color: (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'
						}
					}
				}
			},
			series: [{
				name: 'Brands',
				colorByPoint: true,
				data: [{
					name: 'Chrome',
					y: 61.41,
					sliced: true,
					selected: true
				}, {
					name: 'Internet Explorer',
					y: 11.84
				}, {
					name: 'Firefox',
					y: 10.85
				}, {
					name: 'Edge',
					y: 4.67
				}, {
					name: 'Safari',
					y: 4.18
				}, {
					name: 'Sogou Explorer',
					y: 1.64
				}, {
					name: 'Opera',
					y: 1.6
				}, {
					name: 'QQ',
					y: 1.2
				}, {
					name: 'Other',
					y: 2.61
				}]
			}]				  
		}

	};
	
	var app = new Vue({
	  el: "#app",
	  data() {
		return {
		  intervalId1:'',
		  intervalId2:'',
		  intervalId3:'',
		  stats: null,
		  info: null,
		  loading: true,
		  errored: false,				
		  chartOptions: options,
		  title: '',
		  endpoint: 'https://www.highcharts.com/samples/data/'
		}
	  },
	  watch: {
		title(newValue) {
		  if (newValue === '') {
			this.chartOptions.splineChart.title.text = 'Entire title'
		  } else {
			this.chartOptions.splineChart.title.text = newValue
		  }

		}
	  },
	  filters: {
		currencydecimal (value) {
		  return value.toFixed(2)
		}
	  },			  
	  created () {
		this.loadstock();
		this.loadbpi();
		this.loadstats();
	  },
	  mounted () {
		this.init();
	  },
	  beforeDestroy () {
		   clearInterval(this.intervalId1);
		   clearInterval(this.intervalId2);
		   clearInterval(this.intervalId3);
	  },			  
	  methods :{
		   loadbpi: function() {
				var obj = this;
				obj.loading = true;
			  
				fetch('https://api.coindesk.com/v1/bpi/currentprice.json')
				  .then(
					function(response) {
					  if (response.status !== 200) {
						console.log('Looks like there was a problem. Status Code: ' + response.status);
						obj.errored = true;
						return;
					  }

					  // Examine the text in the response
					  response.json().then(function(data) {
						console.log(data);
						obj.info = data.bpi;
						obj.loading = false;
					  });
					}
				  )
				  .catch(function(err) {
					console.log('Fetch Error :-S', err);
					obj.errored = true;
				  });			  
		   },
		   loadstock: function() {
				var obj = this;
				var names = ['MSFT', 'AAPL', 'GOOG'];
				
				for(var i=0; i<names.length; i++) {
					var name = names[i];
					
					fetchData(this.endpoint + name.toLowerCase() + '-c.json', name, obj);
				}
		   },
		   loadstats: function() {
			   this.stats = {
				   count: [100, 200, 1000],
				   stuck: 10,
				   failed: 5,
				   fraud: 3
			   };
		   },
		   init: function(){          
				const self = this;
				this.intervalId1 = setInterval(function(){
					self.loadbpi();
				}, 5 * 60 * 1000);
				this.intervalId2 = setInterval(function(){
					self.loadstock();
				}, 3 * 60 * 1000);
				this.intervalId3 = setInterval(function(){
					self.loadstats();
				}, 2 * 60 * 1000);
			}
	  }
	});
	