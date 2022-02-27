import numpy as np

class_name = "ArrayTestGenerated" 
nb_slice_tests = 200
nb_reshape_tests = 200
nb_broadcast_tests = 200
    
def random_shape(nb_dims, max_size_dim):
    n = np.random.random_integers(nb_dims)    
    while True:
        res = [np.random.random_integers(max_size_dim) for _ in range(n)]
        if np.prod(res) < 1000:
            return res

def create_random_array(shape):
    return np.random.random_integers(1000, size=shape)

def random_bounds(shape):
    return [(min(a,b), a + 1 if a == b else max(a, b)) for (a, b) in 
            [(np.random.random_integers(d) - 1, np.random.random_integers(d) - 1) for d in shape]]
   
def random_divisor(n):
    while True:
        m = np.random.random_integers(n)
        if n % m == 0: return m
   
def random_reshape(shape):
    size = np.prod(shape)
    d = np.random.random_integers(6)
    res = [1] * d
    i = 0
    while i < d - 1:
        n = random_divisor(size)
        size //= n
        res[i] = n
        i += 1
    res[i] = size
    return res
    
def shape_to_dims(shape):    
    return ",".join([str(d) for d in shape])   
    
def tab(n):
    return " " * 4 * n
    
def no_copy(a, shape):
    try:
        a.shape = shape
        return True
    except AttributeError:
        return False

def init_array(array, array_name):
    res = f"{tab(2)}Array<Integer> {array_name} = new Array<>({shape_to_dims(array.shape)});"
    it = np.nditer(array, flags=['multi_index'])
    while not it.finished:
        res += f" {array_name}.set({it[0]}, {str(it.multi_index).strip('()').rstrip(',')});"
        it.iternext()
    return res + "\n"
    
def create_checksum(array, array_name):
    checksum = 0
    i = 2
    mod = 15485863        
    for v in np.nditer(array):
        checksum += i * v            
        checksum %= mod
        i += 1
    res = f"{tab(2)}int checksum = 0; int i = 2; "
    res += f"for (int v : {array_name}) {{ "
    res += f"checksum += i * v; checksum %= {mod}; ++i; }}\n"
    res += f"{tab(2)}assertEquals({checksum}, checksum);\n"
    return res
        
def generate_slice(array_name1, array_name2):
    shape = random_shape(5, 10)
    a = create_random_array(shape)        
    res = init_array(a, array_name1)        
    bounds = random_bounds(shape)        
    b = a[[slice(a, b) for (a, b) in bounds]]        
    slice_str = "new int[][]{" + ",".join([f"{{{a}, {b}}}" for (a, b) in bounds]) + "}"
    res += f"{tab(2)}Array<Integer> {array_name2} = {array_name1}.slice({slice_str});\n"                
    return a, b, res

def generate_slice_tests():
    res = ""
    for i in range(nb_slice_tests):
        res += f"{tab(1)}@Test\n{tab(1)}void testSlice{i}() {{\n"
        array_name1 = "array1"
        array_name2 = "array2"        
        _, b, code = generate_slice(array_name1, array_name2)
        res += code
        res += create_checksum(b, array_name2)
        res += f"{tab(1)}}}\n"
    return res

def generate_reshape_tests():
    res = ""
    for i in range(nb_reshape_tests):
        res += f"{tab(1)}@Test\n{tab(1)}void testReshape{i}() {{\n"
        array_name1 = "array1"
        array_name2 = "array2"
        _, b, code = generate_slice(array_name1, array_name2)
        res += code
        new_shape = random_reshape(b.shape)                                
        array_name3 = "array3"
        res += f"{tab(2)}Array<Integer> {array_name3} = {array_name2}.reshape({shape_to_dims(new_shape)});\n"                
        c = b.reshape(new_shape)
        res += create_checksum(c, array_name3)
        res += f"{tab(2)}assertEquals({'true' if no_copy(b, new_shape) else 'false'}, {array_name2}.sameUnderlyingArray({array_name3}));\n"
        res += f"{tab(1)}}}\n"
    return res

def can_broadcast(a, b):
    try: 
        a + b
        return True
    except ValueError:
        return False

def generate_broadcast_tests():
    res = ""
    for i in range(nb_broadcast_tests):
        res1 = f"{tab(1)}@Test\n{tab(1)}void testCanBroadcast{i}() {{\n"
        array_name1 = "array1"
        array_name2 = "array2"
        shape1 = random_shape(5, 4)
        shape2 = random_shape(5, 4)
        a = create_random_array(shape1)        
        b = create_random_array(shape2)
        init1 = init_array(a, array_name1)
        init2 = init_array(b, array_name2)
        res1 += init1 + init2
        res1 += f"{tab(2)}boolean canBroadcast = true; try {{ Array.broadcastedDims(array1, array2); }} catch(IllegalArgumentException e) {{ canBroadcast = false; }}\n";
        res1 += f"{tab(2)}assertEquals({'true' if can_broadcast(a, b) else 'false'}, canBroadcast);\n"
        res1 += f"{tab(1)}}}\n"
        res += res1
        if not can_broadcast(a, b): continue
        res2 = f"{tab(1)}@Test\n{tab(1)}void testBroadcast{i}() {{\n"
        res2 += init1 + init2
        c = a + b
        array_name3 = "array3"
        res2 += f"{tab(2)}Array<Integer> {array_name3} = Operator.map2({array_name1}, {array_name2}, (x, y) -> x + y);\n"
        res2 += create_checksum(c, array_name3)
        res2 += f"{tab(1)}}}\n"
        res += res2 
    return res
    
def generate_class_tests():   
    return f"""/* Generated file: do not modify */
package multidimentional;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class {class_name} {{ 
{generate_slice_tests()}
{generate_reshape_tests()}
{generate_broadcast_tests()}
}}"""
    
with open(f"../src/test/multidimentional/{class_name}.java", "w") as f:
    f.write(generate_class_tests())
