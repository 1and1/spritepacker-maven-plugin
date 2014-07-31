Spritepacker Maven Plugin
=========================

This project is a maven plugin to take a set of input images and combine them into a PNG spritesheet. It is capable of generating
plain CSS output with icons names as class names, Less mixins that take the icon name as a parameter, or a
JSON(P) file containing descriptive information about the spritesheet layout that can be plugged into other tools.

Usage
-----

Include the plugin in your maven project, and use the pom.xml configuration directives to tell it what images you want to
combine and where you would like it to combine them to.

	<plugin>
		<groupId>net.oneandone.maven.plugins</groupId>
		<artifactId>spritepacker-maven-plugin</artifactId>
		<version>1.0.0</version>
        <executions>
            <execution>
                <goals>
                    <goal>compile</goal>
                </goals>
                <configuration>
                    <sourceDirectory>${project.basedir}/src/images/sprites/</sourceDirectory>
                    <output>${project.build.directory}/images/sprite.png</output>
                    <includes>
                        <include>*.png</include>
                    </includes>
                    <excludes>
                        <exclude>*.gif</exclude>
                    </excludes>
                    <json>${project.build.directory}/images/sprite.json</json>
                    <jsonpVar>sprite</jsonpVar>
                    <css>${project.build.directory}/images/sprite.css</css>
                    <cssPrefix>icon</cssPrefix>
                    <less>${project.build.directory}/images/sprite.less</less>
                    <lessNamespace>icon</lessNamespace>
                    <padding>10</padding>
                </configuration>

            </execution>
        </executions>
	</plugin>

Configuration
-------------


**sourceDirectory**   
&nbsp;&nbsp;&nbsp;&nbsp; ***(required)*** The directory where your source images reside.  This will be scanned recursively and files included based on
include/exclude rules.

**includes**   
&nbsp;&nbsp;&nbsp;&nbsp; *(optional)* Expression of which files to include.
See [http://plexus.codehaus.org/plexus-utils/apidocs/org/codehaus/plexus/util/DirectoryScanner.html] for more details.

**excludes**   
&nbsp;&nbsp;&nbsp;&nbsp; *(optional)* Expression of which files to exclude.
See [http://plexus.codehaus.org/plexus-utils/apidocs/org/codehaus/plexus/util/DirectoryScanner.html] for more details.

**output**   
&nbsp;&nbsp;&nbsp;&nbsp; ***(required)*** File to write PNG spritesheet to.

**forceOverwrite**   
&nbsp;&nbsp;&nbsp;&nbsp; *(optional, default=false)* Normally the output files are not re-generated if they already exist and none of the source files
are newer than any of the output files. Setting this option to true ensures that all output files are re-created and overwritten
regardless of the source files' last modified dates. This could be especially useful if you want icons that are deleted from the source
directory to always be removed from the spritesheet.

**padding**   
&nbsp;&nbsp;&nbsp;&nbsp; *(optional)* Padding in pixels to be added around each image and the edges of the spritesheet.  Useful if you are having problems
with images bleeding into each other due to users zooming, sub-pixel rendering, etc...

**json**   
&nbsp;&nbsp;&nbsp;&nbsp; *(optional)* File to write JSON(P) spritesheet metadata to. See [below](#json) for structure.

**jsonpVar**   
&nbsp;&nbsp;&nbsp;&nbsp; *(optional)* If set this is used as a padding variable to make the JSON file into a JSONP file which may be more useful depending
on your application. e.g.

    { image: {...} }
    
&nbsp;&nbsp;&nbsp;&nbsp; becomes
    
    jsonpVar = { image: {...} }

**css**   
&nbsp;&nbsp;&nbsp;&nbsp; *(optional)* File to write CSS classes to. See <a href="#css">below</a> for more information on the format.

**cssPrefix**   
&nbsp;&nbsp;&nbsp;&nbsp; *(optional)* Prefix to add to CSS classes. For instance, if there is a "smiley.png" icon it would normally get the CSS class name
".smiley", whereas if the prefix "icon" is specified the resulting class is ".icon-smiley". This helps ensure that icon class names
don't conflict with other CSS classes.

**less**   
&nbsp;&nbsp;&nbsp;&nbsp; *(optional)* File to write Less mixins to. See <a href="#less">below</a> for information on usage.

**lessNamespace**   
&nbsp;&nbsp;&nbsp;&nbsp; *(optional)* The namespace that contains the Less mixins. If this is specified then all icon mixins are put into a #namespace{ }
block, which helps prevent conflicts with other Less mixins.

Output formats
--------------

### CSS

If a CSS output file is specified, the name of each icon file in the source directory becomes a CSS class, optionally prefixed with
the cssPrefix specified in the configuration, that contains position and size information about the icon within the spritesheet. For instance,
an icon with source name "smiley.png", with cssPrefix "icon" specified, would result in the CSS class ".icon-smiley". Note that special
characters are removed and number or hyphens at the beginning of class names are prefixed with an underscore, in keeping with the CSS specification
(see [http://www.w3.org/TR/CSS2/syndata.html#characters] ).

For instance, a source directory containing the following files:

    0smiley.png
    -1frown.png
    +2wink!.png
    excited.png
    
would result in the following CSS:

    ._0smiley{background-position:0 0;width:20px;height:20px;}
    ._-1frown{background-position:-20px 0;width:20px;height:20px;}
    ._2wink{background-position:0 -20px;width:20px;height:20px;}
    .excited{background-position:-20px -20px;width:20px;height:20px;}

### Less

If a Less output files is specified, Less mixins are created for each icon file in the source directory. The resulting mixin file
holds three functions for each icon:

    .create(icon-name) // to create an icon with .pos() and .size()
    .pos(icon-name) // to get the background-position
    .size(icon-name) // to get the size

If you configure lessNamespace these mixins will be surrounded by the chosen Less namespace (#namespace{ }), in which case you can
call the mixins using the following syntax:

    #namespace > .create(icon-name)
    #namespace > .pos(icon-name)
    #namespace > .size(icon-name)

When defining position and size of icons at once, the .create() function can be used, while the .pos() function is useful when
the position of an already-defined icon changes, such as for hover states. For example, to define a hover-state for an icon
within a button, you could do something like this:

    // standard non-hovered icon
    a.button .icon-help {
        #icon > .create(icon-help1);  // define background-position, width and height
    }

    // if only the sprite position changes on hover
    a.button:hover .icon-help {
        #icon > .pos(icon-help2);  // define only background-position
    }

### JSON

If a JSON output file is specified it is formatted as in the example below.  At the top level are keys which are derived
from the file names of the images, with the extension stripped.  For that reason, if you wish to use this data you must
ensure these filenames are unique throughout the entire list of images.

At the next level are the width (*w*), height (*h*), negative x location (*x*), negative y location (*y*), and both x and y
in one for convenience (*xy*) as strings with "px" appended for use in a stylesheet.

At that level is also *n* which contains the same keys (apart from *xy*) with the same values as pure integers. 

	{
		"example" : {
			"w" : "128px",
			"n" : {
				"w" : 128,
				"h" : 128,
				"y" : 10,
				"x" : 398
			},
			"h" : "128px",
			"y" : "-10px",
			"xy" : "-398px -10px",
			"x" : "-398px"
		},
		"anotherImage" : {
			"w" : "378px",
			"n" : {
				"w" : 378,
				"h" : 378,
				"y" : 10,
				"x" : 10
			},
			"h" : "378px",
			"y" : "-10px",
			"xy" : "-10px -10px",
			"x" : "-10px"
		}
	}

This was originally built for use with the official lesscss-maven-plugin with the idea being this plugin creates a spritesheet
and writes the data, and that plugin loads the data and makes it available within the less files.  That's not possible in the
standard lesscss-maven-plugin because it can't read custom JS files however lesscss-java does have the ability to do so, meaning
it was quite simple to add to lesscss-maven-plugin.  There is a forked version available here
[lesscss-maven-plugin with customJs parameter](https://github.com/murphybob/lesscss-maven-plugin) which will allow this.

If you are using that then add *jsonpVar* in the Spritepacker configuration in your pom.xml, and add the output jsonp file as a
*customJsFile* in the lesscss-maven-plugin pom.xml and you will be able to reference sprites in your less files like this:

	.sprite(@sprite){
	  background-image: url(../images/assets-sprite.png);
	  width: ~`Sprites[@{sprite}].w`;
	  height: ~`Sprites[@{sprite}].h`;
	  background-position: ~`Sprites[@{sprite}].xy`;
	}
	
	#demo1 {
	  .sprite("bob");
	  border: 1px solid red;
	}
	
where *Sprites* is the value of jsonpVar.

Notes
-----

This uses Java libraries for creating the spritesheet; it can almost certainly be made smaller by
adding your favourite PNG optimiser (optipng, deflopt, advancepng, etc) downstream in the build process. 

License
-------

See [LICENSE](LICENSE) file.
