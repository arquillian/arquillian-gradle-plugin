
/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.gradle.utils

import org.apache.tools.ant.AntClassLoader
import org.gradle.api.UncheckedIOException

/**
 * Arquillian utilities.
 *
 * @author Benjamin Muschko
 */
class ArquillianUtils {
    /**
     * Performs the closure with local thread context classloader.
     *
     * @param classpathFiles Classpath files
     * @param closure the given closure
     */
    static void withThreadContextClassLoader(Set<File> classpathFiles, Closure closure) {
        ClassLoader originalClassLoader = getClass().classLoader

        try {
            Thread.currentThread().contextClassLoader = createClassLoader(classpathFiles)
            closure()
        }
        finally {
            Thread.currentThread().contextClassLoader = originalClassLoader
        }
    }

    /**
     * Creates the classloader with the given classpath files. The root classloader is {@see AntClassLoader} with a
     * parent-last strategy.
     *
     * @param classpathFiles Classpath files
     * @return URL classloader
     */
    private static URLClassLoader createClassLoader(Set<File> classpathFiles) {
        ClassLoader rootClassLoader = new AntClassLoader(getClass().classLoader, false)
        new URLClassLoader(toURLArray(classpathFiles), rootClassLoader)
    }

    /**
     * Creates URL array from a set of files.
     *
     * @param files Files
     * @return URL array
     */
    private static URL[] toURLArray(Set<File> files) {
        List<URL> urls = new ArrayList<URL>(files.size())

        files.each { file ->
            try {
                urls << file.toURI().toURL()
            }
            catch(MalformedURLException e) {
                throw new UncheckedIOException(e)
            }
        }

        urls.toArray(new URL[urls.size()])
    }

    /**
     * Loads given class from thread context classloader.
     *
     * @param className Class name
     * @return Loaded class
     */
    static Class loadClass(String className) {
        ClassLoader classLoader = Thread.currentThread().contextClassLoader
        classLoader.loadClass(className)
    }
}
