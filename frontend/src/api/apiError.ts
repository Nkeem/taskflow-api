import axios from "axios";

import type { ErrorResponse } from "../types";

export function getApiErrorMessage(error: unknown): string {
  if (axios.isAxiosError<ErrorResponse>(error)) {
    const message = error.response?.data?.message;

    if (message) {
      return message;
    }
  }

  return "Something went wrong";
}
