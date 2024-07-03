cd common

find src/main/java -path src/main/java/com/abiddarris/common/android -prune -o -type f -name "*.java" -print > build/classes.txt

if ! [ -a build/classes ]; then
    mkdir build/classes
fi
echo "Main.java" >> build/classes.txt

javac -d build/classes @build/classes.txt
java -cp build/classes Main