export class ApiQueryError extends Error {
  constructor(response) {
    super(response?.message || 'Request failed');
    this.name = 'ApiQueryError';
    this.response = response;
  }
}

export const unwrapApiResponse = async (request) => {
  const response = await request();

  if (!response.success) {
    throw new ApiQueryError(response);
  }

  return response.data;
};

export const toApiResponse = async (operation) => {
  try {
    const data = await operation();
    return {
      success: true,
      status: 200,
      data,
      error: null,
      timestamp: null,
      message: null,
    };
  } catch (error) {
    if (error instanceof ApiQueryError) {
      return error.response;
    }

    throw error;
  }
};
