var socialInteractionService = (function() {
	function flatten(arrays) {
		return [].concat.apply([], arrays);
	}

	function clusterInteraction(item, interaction) {
		return {
			course: item.courseid,
			source: {
				student: item.student,
				cluster: item.cluster
			},
			target: {
				student: interaction.target,
				cluster: item.cluster
			},
			count: interaction.count,
			avatar: item.avatar,
			name: item.name	
		};
	}

	function outerInteraction(item, interaction, isSource) {
		var a = {
			student: item.student,
			cluster: item.cluster
		};
		var b = {
			student: interaction.target,
			cluster: interaction.cluster
		};
		return {
			course: item.courseid,
			source: isSource ? a : b,
			target: isSource ? b : a,
			count: interaction.count,
			avatar: item.avatar,
			name: item.name	
		};
	}

	function denormalize(clusterInteractions, outerInteractions) {
		return flatten([
			flatten(outerInteractions.map(function(item) {
				return item.interactions.map(function(interaction) {
					var isSource = item.direction === "SOURCE";
					return outerInteraction(item, interaction, isSource);
				});
			})),
			flatten(clusterInteractions.map(function(item) {
				return item.interactions.map(function(interaction) {
					return clusterInteraction(item, interaction);
				});
			}))
		]);
	}

	function unique(array) {
		return array.reduce(function(acc, e) {
			if(acc.indexOf(e) < 0)
				acc.push(e);
			return acc;
		}, []);
	}

	function students(clusterInteractions, outerInteractions) {
		var students = clusterInteractions.map(function(item) {
			return item.student;
		});
		students = students.concat(outerInteractions.map(function(item) {
			return item.student;
		}));
		students = students.concat(flatten(clusterInteractions.map(function(item) {
			return item.interactions.map(function(interaction) {
				return interaction.target;
			});
		})));
		students = students.concat(flatten(outerInteractions.map(function(item) {
			return item.interactions.map(function(interaction) {
				return interaction.target;
			});
		})));
		return unique(students);
	}

	return {
		denormalize: denormalize,
		students : students
	};

})();
