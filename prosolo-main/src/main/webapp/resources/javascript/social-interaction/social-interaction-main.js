$(function () {
	var root = document.getElementById("social-interaction");

	var graphWidth = $(".tab-content").width() / 12 * 9 - 50;
	
	socialInteractionGraph.load({
		host: root.dataset.api,
		courseId : root.dataset.courseId,
		studentId : root.dataset.studentId,
		width : graphWidth,
		height : 640,
		selector : "#social-interaction #graph",
		charge : -60,
		distance : 260,
		clusterMain : "main",
		clusters : ["one", "two", "three", "four", "five", "six"],
		focusMain : {x: $("#social .col-md-9").width() / 2, y: 320},
		focusPoints : [
			{x: 0, y: 0},
			{x: graphWidth, y: 0},
			{x: 0, y: 640},
			{x: graphWidth, y: 640}
		],
		relations : [
			{ lower: 0, upper: 33, type: "twofive" },
			{ lower: 33, upper: 66, type: "fivezero" },
			{ lower: 66, upper: 85, type: "sevenfive" },
			{ lower: 85, upper: 100, type: "onezerozero" }	
		],
		onNodeClick : function(student) {
			$("#social-interactions-nonodeselected").hide();
			$("#social-interactions-selectedstudent").show();
			$("#social-interactions-by-type-panel").show();
			$("#social-interactions-by-peer-panel").show();
			$("#social-interactions-selected-id").text(student.id);
			$("#social-interactions-selected-name").text(student.name);
			$("#social-interactions-selected-cluster").text(student.cluster);
			$("#social-interactions-selected-avatar").attr("src", student.avatar);
			$("#social-interactions-selected-avatar").show();
			initializeDataForSelectedStudent(student,root.dataset.courseId);
		},
		noResultsMessage: "No results found for given parameters.",
		systemNotAvailableMessage: "System is not available."
	});
var getStudentsData=function(peers){
	return $.ajax({
		url : "http://" + root.dataset.api + "/api/social/interactions/data",
		data : {"students" : peers},
		type : "GET",
		crossDomain: true,
		dataType: 'json'
	});
}
	var initializeDataForSelectedStudent=function(student, courseid){
		$.ajax({
			url : "http://" + root.dataset.api + "/api/social/interactions/interactionsbypeers/"+courseid+"/"+student.id,
			//data : {"students" : part},
			type : "GET",
			crossDomain: true,
			dataType: 'json'
		}).done(function(data){
			for(var i=0;i<data[0].interactions.length;i++){
				data[0].interactions[i]=JSON.parse(data[0].interactions[i]);
			}
			var interactions=data[0].interactions;
			var peersinteractions={};
			var peers=[];
			if(interactions.length>0){
				interactions.forEach(function(interaction){
					var intobject={};
					if(typeof (peersinteractions[interaction.peer])!=='undefined'){
							intobject=peersinteractions[interaction.peer];
					}else peers.push(interaction.peer);
					intobject[interaction.direction]={count:interaction.count, percentage: Math.round(interaction.percentage*100)}
					peersinteractions[interaction.peer]=intobject;
				});
				var studentsData=getStudentsData(peers);
					$.when(
					getStudentsData(peers))
					.then(function(studentsData){
							$("#social-interactions-bypeer-table").remove();
						var innerHtml="<table id='social-interactions-bypeer-table'><tr style='font-weight:bold'><td style='width:120px'>Student</td><td style='width:80px'>OUT</td><td style='width:80px'>IN</td></tr>";

						for(var peerId in peersinteractions){
							var interaction=peersinteractions[peerId];
							var student=studentsData[peerId];
							innerHtml=innerHtml+"<tr><td>"+student.name+"</td>"
							if(typeof(interaction.OUT)!=='undefined')
								innerHtml=innerHtml+"<td>"+interaction.OUT.count+"("+interaction.OUT.percentage+" %)</td>";
							if(typeof(interaction.IN)!=='undefined')
								innerHtml=innerHtml+"<td>"+interaction.IN.count+"("+interaction.IN.percentage+" %)"+"</td>";


						};


						innerHtml=innerHtml+"</table>";
								$("#social-interactions-bypeers").append(innerHtml);
						$("social-interactions-by-peer-panel").show();

					});


			}
		});

		$.ajax({
			url : "http://" + root.dataset.api + "/api/social/interactions/interactionsbytype/"+courseid+"/"+student.id,
			//data : {"students" : part},
			type : "GET",
			crossDomain: true,
			dataType: 'json'
		}).done(function(data){
			for(var i=0;i<data[0].interactions.length;i++){
				data[0].interactions[i]=JSON.parse(data[0].interactions[i]);
			}
			var interactions=data[0].interactions;
			if(interactions.length>0){
				$("#social-interactions-bytype-table").remove();
				var innerHtml="<table id='social-interactions-bytype-table'><tr style='font-weight:bold'><td style='width:100px'>Type</td><td style='width:80px'>OUT</td><td style='width:80px'>IN</td></tr>";
				interactions.forEach(function(interaction){
						innerHtml=innerHtml+"<tr><td>"+interaction.type+"</td><td>"
						+interaction.fromusercount+"("+Math.round(interaction.fromuserpercentage*100)+" %)</td><td>"
						+interaction.tousercount+"("+Math.round(interaction.touserpercentage*100)+" %)"+
						"</td>";


				});
				innerHtml=innerHtml+"</table>";
				$("#social-interactions-bytype").append(innerHtml);
				$("social-interactions-by-type-panel").show();

			}

		});
	};

});
