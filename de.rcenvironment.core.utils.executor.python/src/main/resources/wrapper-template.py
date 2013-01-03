import base64
import sys
import tempfile

_rce_ = True # for scripts that need to detect if they are run from inside RCE (or standalone, if not defined)
_dm_ = {} # datamanagement dictionary string-key -> filename

def ___dims___(a):
    ''' Determine dimensions of array '''
    dimensions = []
    x = list(a)
    while True:
        try:
            dimensions += [len(x)]
            if type(x[0]) in [type([]), type(())]:
                x = tuple(x[0])
            else:
                break
        except:
            break
    return dimensions

def ___array___(name, a):
    ''' Determine dimension sizes. Literal 1L is no longer valid in python 3 (only type"int"), therefore 10000000000 '''
    types = {type(""): "String", type(1): "Integer", type(10000000000): "Integer", type(1.0): "Real", type(True): "Logic", type(None): "Empty" }
    dimensions = ___dims___(a)
    dims = len(dimensions)
    code = ''
    indent = '    '
    for i, dim in enumerate(dimensions):
        code += indent * i
        code += "for a%d in xrange(%d):\n" % (i, dim)
    code += indent * dims
    proxy = ','.join(["%d" for x in xrange(dims)]) # generates %d,%d,%d,...
    index = ','.join(["a%d" % x for x in xrange(dims)]) # generates "a0,a1,a2...,an"
    index2 = ''.join(["[a%d]" % x for x in xrange(dims)]) # generates "[a0][a1][a2]...[an]"
    code += 'arr += [base64.b64encode("_A_0_R_%s_A_0_R_%%s_A_0_R_%%s" %% (%s, types[type(%s%s)], %s%s))]\n' % (proxy, index, name, index2, name, index2)
    arr = [] # output array reference for back channeling
    exec(code, {}, {"arr": arr, "types": types, "base64": base64, name: a}) # as locals
    return arr

def ___my_init___():
    ''' Generated binding code goes here '''
    global _dm_
    pass # init

def ___my_cleanup___():
    ''' Generated binding code goes here '''
    global _dm_ # return all values to store in data management, keys being names of filenames and values being plain strings or strings containing absolute file paths
    pass # cleanup
    for k, v in _dm_.iteritems():
        print >> sys.stderr, "_D_0_M_" + base64.b64encode(str(k) + "_D_0_M_" + str(v))

# Ensure that os._exit() works
try:
    import os
    ___os_exit___ = os._exit
    def ___my_os_exit___(n):
        print("Exiting from os._exit(" + str(n) + ")")
        ___my_cleanup___()
        ___os_exit___(n)
    os._exit = ___my_os_exit___
except Exception:
    pass

# Ensure that thread.exit() works (what are the semantics of thread-global vars?)
try:
    import thread
    ___thread_exit___ = thread.exit
    def ___my_thread_exit___():
        print("Exiting from thread.exit()")
#        ___my_cleanup___()
        ___thread_exit___()
    thread.exit = ___my_thread_exit___
except Exception:
    pass

# Now follows the main routine
try:
    ___my_init___()
    run = pass # main
    exec(run) # this avoids leading spaces in wrapped code
    ___my_cleanup___()
except SystemExit:
    print("Exiting from SystemExit exception")
    ___my_cleanup___()
