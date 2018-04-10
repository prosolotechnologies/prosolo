function loadSocialInteractionGraph() {

    var root = document.getElementById("socialInteractionData");

    socialInteractionGraph.load({
        host: root.dataset.api,
        courseId: root.dataset.courseId,
        studentId: root.dataset.studentId,
        studentName: root.dataset.studentName,
        studentAvatar: root.dataset.studentAvatar,
        graphContainerId: "graph",
        charge: -60,
        distance: 260,
        clusterMain: "main",
        clusters: ["one", "two", "three", "four", "five", "six"],
        focusMain: {x: $("#social .col-md-9").width() / 2, y: 320},
        focusPoints: [
            {x: 0, y: 0},
            {x: root.offsetWidth, y: 0},
            {x: 0, y: root.offsetHeight},
            {x: root.offsetWidth, y: root.offsetHeight}
        ],
        relations: [
            {lower: 0, upper: 33, type: "twofive"},
            {lower: 33, upper: 66, type: "fivezero"},
            {lower: 66, upper: 85, type: "sevenfive"},
            {lower: 85, upper: 100, type: "onezerozero"}
        ],
        systemNotAvailableMessage: "No data to render the graph."
    });

}