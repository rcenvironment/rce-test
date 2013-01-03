import sys
import os
import doctest
import tempfile
import subprocess

if sys.version_info < (2, 6):
    print "Use Python 2.6.x or higher to run test cases (dependency for temp file creation)"
    exit()

# Necessary constants
f = open('./wrapper-template.py', 'r')
template = f.read()
f.close()

# injected wrapper pre code
initCode = """    global a
    global b
    a = 1
    b = 2
    print "Init a =", a
    print "Init b =", b
"""

# injected wrapper post code
cleanupCode = """    global a
    global b
    print "Return a =", a
    print "Return b =", b
"""

### Helper definitions
def create_temp_py_file(code):
    f = tempfile.NamedTemporaryFile(mode = 'w', delete=False)
    content = template.replace('    pass # init', initCode)
    content = content.replace('    pass # cleanup', cleanupCode)
    content = content.replace('    pass # main', code)
    f.write(content)
    f.close()
    return f.name

def remove_temp_py_file(filename):
    os.unlink(filename)

def execute_script(filename):
    process = subprocess.Popen('python ' + filename, shell=True, bufsize=-1, stdout=subprocess.PIPE)
    pipe = process.stdout
    process.wait()
    content = pipe.read()
    pipe.close()
    return content

### Now all test cases follow
def test_os_exit():
    ''' Test if the immediate os._exit(n) is caught
    >>> test_os_exit() #doctest: +NORMALIZE_WHITESPACE 
    Init a = 1
    Init b = 2
    Main script here
    Exiting from os._exit(1)
    Return a = 1
    Return b = 2
    '''
    code = """    import os; print "Main script here"; os._exit(1)""" + "\n"
    filename = create_temp_py_file(code)
    print(execute_script(filename))
    remove_temp_py_file(filename)
    
def test_sys_exit():
    ''' Test if the standard sys.exit(n) is caught
    >>> test_sys_exit() #doctest: +NORMALIZE_WHITESPACE 
    Init a = 1
    Init b = 2
    Main script here
    Exiting from SystemExit exception
    Return a = 1
    Return b = 2
    '''
    code = """    import sys; print "Main script here"; sys.exit(1)""" + "\n"
    filename = create_temp_py_file(code)
    print(execute_script(filename))
    remove_temp_py_file(filename)
    
def test_thread_exit():
    ''' Test if thread.exit() is caught
    >>> test_thread_exit() #doctest: +NORMALIZE_WHITESPACE 
    Init a = 1
    Init b = 2
    Main script here
    Exiting from thread.exit()
    Exiting from SystemExit exception
    Return a = 1
    Return b = 2
    '''
    code = """    import thread; print "Main script here"; thread.exit()""" + "\n"
    filename = create_temp_py_file(code)
    print(execute_script(filename))
    remove_temp_py_file(filename)
    
def test_builtin_exit():
    ''' Test if __builtins__.exit() is caught
    >>> test_builtin_exit() #doctest: +NORMALIZE_WHITESPACE 
    Init a = 1
    Init b = 2
    Main script here
    Exiting from SystemExit exception
    Return a = 1
    Return b = 2
    '''
    code = """    print "Main script here"; exit(1)""" + "\n"
    filename = create_temp_py_file(code)
    print(execute_script(filename))
    remove_temp_py_file(filename)
    
def test_builtin_quit():
    ''' Test if __builtins__.quit() is caught
    >>> test_builtin_quit() #doctest: +NORMALIZE_WHITESPACE 
    Init a = 1
    Init b = 2
    Main script here
    Exiting from SystemExit exception
    Return a = 1
    Return b = 2
    '''
    code = """    print "Main script here"; exit(1)""" + "\n"
    filename = create_temp_py_file(code)
    print(execute_script(filename))
    remove_temp_py_file(filename)
    
def test_system_exit():
    ''' Test if SystemExit exception is caught
    >>> test_system_exit() #doctest: +NORMALIZE_WHITESPACE 
    Init a = 1
    Init b = 2
    Main script here
    Exiting from SystemExit exception
    Return a = 1
    Return b = 2
    '''
    code = """    print "Main script here"; raise SystemExit""" + "\n"
    filename = create_temp_py_file(code)
    print(execute_script(filename))
    remove_temp_py_file(filename)
    

if __name__ == "__main__":
    import doctest
    doctest.testmod()
