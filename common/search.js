var index = lunr(function () {
    this.use(lunr.multiLanguage('en', 'ua'));

    this.field('content')
    this.ref('path')

    data.forEach(function (post) {
        this.add(post)
    }, this)
})