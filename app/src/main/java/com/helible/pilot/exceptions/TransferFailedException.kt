package com.helible.pilot.exceptions

import java.io.IOException

class TransferFailedException : IOException("Reading incoming data failed")