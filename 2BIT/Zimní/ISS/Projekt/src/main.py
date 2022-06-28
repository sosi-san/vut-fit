import numpy as np
import matplotlib.pyplot as plt
import soundfile as sf
import IPython
import sys
from scipy.signal import spectrogram, lfilter, freqz, tf2zpk
np.warnings.filterwarnings('ignore', category=np.VisibleDeprecationWarning)
PRAH = 34

def center_clipping(signal):
    
    maximum = abs(signal.max()*0.7)
    #minimum = signal.min()*0.7

    s_clip = []
    for i in range(len(signal)):
        if signal[i] > maximum:
            s_clip.append(1)
        elif signal[i] < -maximum:
            s_clip.append(-1)
        else:
            s_clip.append(0)

    return np.array(s_clip)

def auto_corelation(signal_x):    
    N = signal_x.size
    result = []
    for k in range(0, N-1):
        value = 0
        for n in range(N):
            value += signal_x[n]*signal_x[n-k]
            if n == k:
                break
        result.append(value)
        
    s_seg = np.array(result)
    s_seg = s_seg[::-1]
    
    k = np.arange(0, N-1)
    lag_value = s_seg[PRAH:].max()
    lag_index = PRAH + s_seg[PRAH:].argmax()
    return s_seg, lag_value, lag_index, k

def plot_graph(x, y, title, lable_x, lable_y, nazev):
    plt.figure(figsize=(9,3))
    plt.plot(x, y)
    plt.gca().set_title(title)
    plt.gca().set_ylabel(lable_y)
    plt.tight_layout()
    plt.savefig('img/'+nazev+'.pdf')

def load_signal(file_name):
    s, fs = sf.read(file_name + '.wav')
    s -= np.mean(s) # normalizace
    s /= np.abs(s).max() # ustředění 
    return s, fs
    
def create_segment(s, fs, seg_from, seg_len):
    seg_samples_from = int(seg_from * fs)         # začátek segmentu ve vzorcích
    seg_samples_to = int((seg_from + seg_len) * fs) # konec segmentu ve vzorcích

    s_seg = s[seg_samples_from:seg_samples_to]
    s_seg_t = np.arange(s_seg.size) / fs + seg_from
    return np.array(s_seg), s_seg_t
def signal_to_segments(s, fs, pre, posun):
    s_seg = []
    for i in range(99):
        s_par_seg, s_par_seg_t = create_segment(s, fs, i*pre, posun)
        s_seg.append(s_par_seg)
    return np.array(s_seg)

def segments_to_f0(s_seg, fs):
    s_f0 = []
    for i in range(len(s_seg)):
        s_clip = center_clipping(s_seg[i])
        s_auto, lag_value, lag_index, k = auto_corelation(s_clip)
        s_f0.append((1/(lag_index))*16000)
    return np.array(s_f0)

def segment_to_fft(s_seg):
    s_fft = []
    for i in range(len(s_seg)):
        tmp = np.fft.fft(s_seg[i], 1024)
        log_ramce = 10*np.log10(abs(tmp)**2+1e-20)
        s_fft.append(log_ramce)
    return np.array(s_fft)

def dft(s_seg):
    result = []
    N = 1024
    s_seg_n = np.zeros(N)
    s_seg_n = np.array(s_seg_n)
    for i in range(len(s_seg)):
            s_seg_n[i] = s_seg[i]
    for k in range(len(s_seg_n)-1):
        value = 0
        for n in range(len(s_seg_n)):
            value += s_seg_n[n]*np.exp(-2j * np.pi * k * n / N)
        result.append(value)      
    return np.array(result)

def idft(s_seg):
    s_fft = []
    N = 1024
    for n in range(len(s_seg)-1):
        value = 0
        for k in range(len(s_seg)):
            value += s_seg[k]*np.exp(2j * np.pi * k * n / N)
        value = 1/N*value
        s_fft.append(value)
    return np.array(s_fft)

def my_segment_to_dft(s_seg):
    s_fft = []
    for i in range(len(s_seg)):
        tmp = dft(s_seg[i])
        log_ramce = 10*np.log10(abs(tmp)**2+1e-20)
        s_fft.append(log_ramce)
    return np.array(s_fft)


# ----------------------------------------------------------------------------------- Ukol 3
# Maskon
file_name = "../audio/maskon_tone"
s_maskon, fs_maskon = load_signal(file_name)
start = 0
s_maskon_sec = s_maskon[start:start+16000]
s_seg_maskon = signal_to_segments(s_maskon_sec, fs_maskon, 0.01, 0.02)


# Vytvoření grafu
plot_graph(np.arange(s_seg_maskon[0].size) / fs_maskon, s_seg_maskon[0], 'ramec ' + file_name, '$t[s]$', 'signal', 'maskon_ramec')

# Maskoff
file_name = "../audio/maskoff_tone"
s_maskoff, fs_maskoff = load_signal(file_name)
start = 8000
s_maskoff_sec = s_maskoff[start:start+16000]
s_seg_maskoff = signal_to_segments(s_maskoff_sec, fs_maskoff, 0.01, 0.02)

# Vytvoření grafu
plot_graph(np.arange(s_seg_maskoff[0].size) / fs_maskoff, s_seg_maskoff[0], 'ramec ' + file_name, '$t[s]$', 'signal', 'maskoff_ramec')

# ----------------------------------------------------------------------------------- Ukol 4

# Center clipping
s_clip_maskon = center_clipping(s_seg_maskon[0])
s_clip_maskon_t = np.arange(s_clip_maskon.size) / fs_maskon
# Vytvoření grafu
plot_graph(s_clip_maskon_t, s_clip_maskon, 'Centrální klipování s 70% ' + ' maskon', '$t[s]$', 'signal', 'maskon_clip')

# Autokorelace
s_auto_maskon, lag_value, lag_index, k = auto_corelation(s_clip_maskon)

# Vytvoření grafu
point1 = [lag_index, 0]
point2 = [lag_index, lag_value]
x_values = [point1[0], point2[0]]
y_values = [point1[1], point2[1]]

plt.figure(figsize=(9,3))
plt.plot(k, s_auto_maskon)
plt.gca().set_title('Autokorelace maskon')
plt.gca().set_ylabel('signal')
plt.gca().axvline(x=32, color='r', label='Prah')
plt.gca().plot(x_values, y_values)
plt.gca().scatter(lag_index, lag_value, color='y', label='Lag')
plt.gca().legend()
plt.tight_layout()
plt.savefig('img/maskon_autokorelace.pdf')

# Základní frekvence

s_f0_maskon = segments_to_f0(s_seg_maskon, fs_maskon)
s_f0_maskoff = segments_to_f0(s_seg_maskoff, fs_maskoff)
plt.figure(figsize=(9,3))
#plt.plot(np.arange(s_f0_maskon.size/2), s_f0_maskon[int(s_f0_maskon.size/2):], label='maskon')
#plt.plot(np.arange(s_f0_maskoff.size/2), s_f0_maskoff[int(s_f0_maskoff.size/2):], label='maskoff')
plt.plot(np.arange(s_f0_maskon.size), s_f0_maskon, label='maskon')
plt.plot(np.arange(s_f0_maskoff.size), s_f0_maskoff, label='maskoff')
plt.gca().set_title('Zakladni frekvence ramce')
plt.gca().set_ylabel('')
plt.gca().legend()
plt.tight_layout()
plt.savefig('img/zakladni_f0.pdf')

an = np.mean(s_f0_maskoff)
stdn = np.var(s_f0_maskoff)
print('Středni hodnota maskoff: ' + str(an))
print('Rozptyl maskoff: '  + str(stdn))
an = np.mean(s_f0_maskon)
stdn = np.var(s_f0_maskon)
print('Středni hodnota maskon: ' + str(an))
print('Rozptyl maskon: ' + str(stdn))

# ----------------------------------------------------------------------------------- Ukol 5

s_fft_maskon = segment_to_fft(s_seg_maskon)
s_fft_maskon = np.array(s_fft_maskon)[:,0:512]
s_fft_maskon = s_fft_maskon.T
s_fft_maskon_spec = s_fft_maskon[::-1]

# s_fft_maskon = my_segment_to_dft(s_seg_maskon) vlastni implementace dft

plt.figure(figsize=(9,3))
plt.imshow(s_fft_maskon_spec,extent=[0,1,0,8000],interpolation = "nearest",origin="upper",aspect="auto")
plt.gca().set_title('Spectogram s rouškou')
cbar = plt.colorbar()
plt.tight_layout()
plt.savefig('img/maskon_spec.pdf')



s_fft_maskoff = segment_to_fft(s_seg_maskoff)
s_fft_maskoff = np.array(s_fft_maskoff)[:,0:512]
s_fft_maskoff = s_fft_maskoff.T
s_fft_maskoff_spec = s_fft_maskoff[::-1]


# s_fft_maskoff = my_segment_to_dft(s_seg_maskoff) vlastni implementace dft

plt.figure(figsize=(9,3))
plt.imshow(s_fft_maskoff_spec,extent=[0,1,0,8000],interpolation = "nearest",origin="upper", aspect="auto")
plt.gca().set_title('Spectogram bez rouškou')
cbar = plt.colorbar()
plt.tight_layout()
plt.savefig('img/maskoff_spec.pdf')
# ----------------------------------------------------------------------------------- Ukol 6



"""
Y = s_fft_maskon
X = s_fft_maskoff
for i in range(len(s_fft_maskon)-1):
    value = 0
    H.append(np.divide(Y[i], X[i]))
H = np.array(H)
"""
H = np.divide(s_fft_maskon, s_fft_maskoff)
H = np.array(H)

s_fft_filtr = []
for i in range(len(H)-1):
    b = np.mean(H[i])
    b = 10*np.log10(abs(b)**2)
    s_fft_filtr.append(b)

s_fft_filtr = np.array(s_fft_filtr)

plot_graph(np.arange(s_fft_filtr.size), s_fft_filtr, 'Frekvenční charakteristika roušky', '', '', 'f_chara')

# ----------------------------------------------------------------------------------- Ukol 7

s_ifft = np.fft.ifft(s_fft_filtr)
#my_s_ifft = idft(s_fft_filtr)

plot_graph(np.arange(s_ifft.size), s_ifft.real, 'Impulzní odezva roušky', '', '', 'i_odezva')

#plot_graph(np.arange(my_s_ifft.size), my_s_ifft.real, 'Moje Impulzní odezva roušky', '', '', 'i_odezva')
# ----------------------------------------------------------------------------------- Ukol 8





plt.figure(figsize=(9,3))

simulated = lfilter(s_ifft.real, [1.0], s_maskoff)


plt.plot(np.arange(simulated.size) / fs_maskon, simulated, label='simulated')
plt.plot(np.arange(s_maskon.size) / fs_maskon, s_maskon, label='maskon')

sf.write('../audio/sim_maskon_tone.wav', simulated, fs_maskon)

plt.gca().set_xlabel('$vzorky$')
plt.gca().set_title('Porovani signalu ton')
plt.gca().set_ylabel('signal')
plt.gca().legend()
plt.tight_layout()

plt.savefig('img/ton_sim.pdf')

file_name = "../audio/maskon_sentence"
s_maskon_sentence, fs_maskon_sentence = load_signal(file_name)

file_name = "../audio/maskoff_sentence"
s_maskoff_sentence, fs_maskoff_sentence = load_signal(file_name)

plt.figure(figsize=(9,3))

simulated = lfilter(s_ifft.real, [1.0], s_maskoff_sentence)


plt.plot(np.arange(simulated.size)/ fs_maskoff_sentence, simulated, label='simulated')
plt.plot(np.arange(s_maskon_sentence.size) / fs_maskon_sentence, s_maskon_sentence, label='maskon')
plt.plot(np.arange(s_maskoff_sentence.size) / fs_maskoff_sentence, s_maskoff_sentence, label='maskoff')

sf.write('../audio/sim_maskon_sentence.wav', simulated, fs_maskon_sentence)

plt.gca().set_title('Porovani signalu veta')
plt.gca().set_ylabel('signal')
plt.gca().legend()
plt.tight_layout()
plt.savefig('img/veta_sim.pdf') 




# ----------------------------------------------------------------------------------- Ukol 12

file_name = "../audio/maskon_tone"
s_maskon, fs_maskon = load_signal(file_name)
start = 0
s_maskon_sec = s_maskon[start:start+16000]
s_seg_maskon_12 = signal_to_segments(s_maskon_sec, fs_maskon, 0.015, 0.025)



# Maskoff
file_name = "../audio/maskoff_tone"
s_maskoff, fs_maskoff = load_signal(file_name)
start = 8000
s_maskoff_sec = s_maskoff[start:start+16000]
s_seg_maskoff_12 = signal_to_segments(s_maskoff_sec, fs_maskoff, 0.015, 0.025)


s_seg_maskon_12_seg = s_seg_maskon_12[0]
s_seg_maskoff_12_seg = s_seg_maskoff_12[0]
s_seg_maskon_12_seg = s_seg_maskon_12_seg[0:320]
s_seg_maskoff_12_seg = s_seg_maskoff_12_seg[0:320]
    



s_clip_maskon = []
s_clip_maskoff = []
for i in range(len(s_seg_maskon_12)):
    if s_seg_maskon_12[i].size > 0:
        s_clip_maskon.append(center_clipping(s_seg_maskon_12[i]))
        s_clip_maskoff.append(center_clipping(s_seg_maskoff_12[i]))
    
s_clip_maskon = np.array(s_clip_maskon)
s_clip_maskoff = np.array(s_clip_maskoff)



segs_on_off_15_col = []
segs_off_on_15_col = []


for i in range(len(s_clip_maskoff)):

    segs_off_on_15_col = np.correlate(s_clip_maskoff[i],s_clip_maskon[i],mode="full")
    segs_on_off_15_col = np.correlate(s_clip_maskon[i],s_clip_maskoff[i],mode="full")

    segs_off_on_15_col = np.array(segs_off_on_15_col)
    segs_on_off_15_col = np.array(segs_on_off_15_col)

    segs_off_on_15_col = segs_off_on_15_col[len(segs_off_on_15_col)//2:]
    segs_on_off_15_col = segs_on_off_15_col[len(segs_on_off_15_col)//2:]

    arg_on_off = segs_off_on_15_col.argmax()
    arg_off_on = segs_on_off_15_col.argmax()

    arg_on_off = np.clip(arg_on_off,-80,80)
    arg_off_on = np.clip(arg_off_on,-80,80)

    s_off = s_seg_maskoff_12[i]
    s_on = s_seg_maskon_12[i]
    
    shift = 0
    if(arg_off_on<arg_on_off):
        shift = arg_off_on
        s_seg_maskoff_12[i] = s_off[0:len(s_off)-shift]
        s_seg_maskon_12[i] = s_on[shift:len(s_on)]
    else:
        shift = arg_on_off
        s_seg_maskoff_12[i] = s_off[shift:len(s_off)]
        s_seg_maskon_12[i] = s_on[0:len(s_on)-shift]
        
    s_off = s_seg_maskoff_12[i]
    s_on = s_seg_maskon_12[i]
    
    s_seg_maskoff_12[i] = s_off[0:320]
    s_seg_maskon_12[i] = s_on[0:320]

plt.figure(figsize=(9,3))
plt.plot(s_seg_maskon_12_seg, label="maskon")
plt.plot(s_seg_maskoff_12_seg, label="maskoff")
plt.gca().set_title("Rámce před zarovnáním")
plt.gca().legend()
plt.tight_layout()
plt.savefig('img/ramec_pred_15.pdf')




plt.figure(figsize=(9,3))
plt.plot(s_seg_maskon_12[0], label="maskon")
plt.plot(s_seg_maskoff_12[0], label="maskoff")
plt.gca().set_title("Rámce po zarovnání")
plt.gca().legend()
plt.tight_layout()
plt.savefig('img/ramec_po_15.pdf')



# ----------------------------------------------------------------------------------- Ukol 13

same_f0_on = []
same_f0_off = []
value = 0
for i in range(len(s_f0_maskon)):
    if s_f0_maskon[i] == s_f0_maskoff[i]:
        same_f0_on.append(s_seg_maskon[i])
        same_f0_off.append(s_seg_maskoff[i])


same_f0_on = np.array(same_f0_on)
same_f0_off = np.array(same_f0_off)

        
same_f0_on = segment_to_fft(same_f0_on)
same_f0_on = np.array(same_f0_on)[:,0:512]
same_f0_on = same_f0_on.T

same_f0_off = segment_to_fft(same_f0_off)
same_f0_off = np.array(same_f0_off)[:,0:512]
same_f0_off = same_f0_off.T

H = np.divide(same_f0_on, same_f0_off)
H = np.array(H)

s_fft_filtr_same = []
for i in range(len(H)):
    b = np.mean(H[i])
    b = 10*np.log10(abs(b)**2)
    s_fft_filtr_same.append(b)

s_fft_filtr_same = np.array(s_fft_filtr_same)



          
plt.figure(figsize=(9,3))
plt.plot(np.arange(s_fft_filtr.size), s_fft_filtr, label='jiné f0')
plt.plot(np.arange(s_fft_filtr_same.size), s_fft_filtr_same, label='stejné f0')
plt.gca().set_title('Frekvenční charakteristika')
plt.gca().set_ylabel('')
plt.gca().legend()
plt.tight_layout()
plt.savefig('img/zakladni_f0_13.pdf')

s_ifft = np.fft.ifft(s_fft_filtr_same)

simulated = lfilter(s_ifft.real, [1.0], s_maskoff)
sf.write('../audio/sim_maskon_tone_only_match.wav', simulated, fs_maskon)
simulated = lfilter(s_ifft.real, [1.0], s_maskoff_sentence)
sf.write('../audio/sim_maskon_sentence_only_match.wav', simulated, fs_maskon_sentence)

