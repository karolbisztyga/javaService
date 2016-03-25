//predefined variables
var debugging = true;
var assetsSrc = './';
var assetsDest = '../WEB-INF/';

// Include gulp
var gulp = require('gulp'); 

// Include Our Plugins
var jshint = require('gulp-jshint');
var sass = require('gulp-sass');
var concat = require('gulp-concat');
var uglify = require('gulp-uglify');
var rename = require('gulp-rename');
var jade = require('gulp-jade');
var gutil = require('gulp-util');


var onError = function(error) {
    console.log(error.message);
    gutil.beep();
};

// Lint Task
gulp.task('lint', function() {
    return gulp.src(assetsSrc + 'scripts/*.js')
        .on('error',onError)
        .pipe(jshint())
        .pipe(jshint.reporter('default'));
});

// Compile Our Sass
gulp.task('sass', function() {
    return gulp.src(assetsSrc + 'stylesheets/*.scss')
        .pipe(sass())
        .on('error',onError)
        .pipe(gulp.dest(assetsDest + 'css'));
});

var scripts = [
        "global",
        "findUser"];

for(var i=0;i<scripts.length;++i) {
    // Concatenate & Minify JS
    var script = scripts[i];
    (function(script) {
        gulp.task('script-' + script, function() {
            var g = gulp.src(assetsSrc + 'scripts/'+ script +'.js').pipe(rename(script + '.js'));
                if(!debugging) {
                    g.pipe(uglify());
                }
                g.pipe(gulp.dest(assetsDest+'js'));
                g.on('error',onError);
            return g;
        });
    })(script);
    scripts[i] = 'script-' + script;
}
scripts[scripts.length] = 'lint';

gulp.task('jade',function() {
    gulp.src([
            assetsSrc + 'views/*.jade',
            '!'+ assetsSrc + 'views/template.jade',
            '!'+ assetsSrc + 'views/includes/*.jade'])
    .pipe(jade({pretty: true}))
    .on('error',onError)
    .pipe(gulp.dest(assetsDest));
});

// Watch Files For Changes
gulp.task('watch', function() {
    gulp.watch(assetsSrc + 'scripts/*.js', scripts);
    gulp.watch(assetsSrc + 'stylesheets/*.scss', ['sass']);
    gulp.watch([assetsSrc + 'views/*.jade', assetsSrc + 'views/includes/*.jade'], ['jade']);
});

var tasks = ['lint', 'sass', 'watch','jade'];
for(var i=0;i<scripts.length;++i) {
    tasks[tasks.length] = scripts[i];
}

// Default Task
gulp.task('default', tasks);