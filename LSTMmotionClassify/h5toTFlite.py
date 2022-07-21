import tensorflow as tf
from tensorflow import keras
"""
model = keras.models.load_model('best_model.h5')
model.save("h5_to_pb", save_format="tf")
"""
saved_model_dir = "h5_to_pb"
converter = tf.lite.TFLiteConverter.from_saved_model(saved_model_dir)
converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS,tf.lite.OpsSet.SELECT_TF_OPS]
tflite_model = converter.convert()
open('pb_to_tflite/converted_model.tflite','wb').write(tflite_model)
