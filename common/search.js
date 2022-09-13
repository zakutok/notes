var index = lunr(function () {
    this.field('content')
    this.ref('path')

    data.forEach(function (post) {
        this.add(post)
    }, this)
})